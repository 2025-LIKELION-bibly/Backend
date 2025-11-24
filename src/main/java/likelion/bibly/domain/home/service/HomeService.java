package likelion.bibly.domain.home.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.assignment.service.AssignmentService;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.bookmark.dto.BookmarkListResponse;
import likelion.bibly.domain.bookmark.service.BookmarkService;
import likelion.bibly.domain.bookshelf.dto.TraceGroupResponse;
import likelion.bibly.domain.bookshelf.dto.TraceItemResponse;
import likelion.bibly.domain.bookshelf.service.BookShelfService;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.home.dto.HomeResponse;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;
    private final AssignmentService assignmentService;
    private final BookShelfService bookShelfService;
    private final BookmarkService bookMarkService;
    private final ReadingSessionRepository readingSessionRepository;
    private final BookRepository bookRepository;


    public HomeResponse getHomeData(Long memberId, Long groupId) {

        // 1. 그룹 정보 조회 및 유효성 검사
        Group currentGroup = groupRepository.findById(groupId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

        // 2. 모임 참여 멤버 닉네임 목록 조회
        List<Member> groupMembers = memberRepository.findAllByGroup_GroupId(groupId);
        List<String> memberNicknames = groupMembers.stream()
                .map(Member::getNickname)
                .collect(Collectors.toList());

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        String userId = member.getUserId();

        // A. 현재 읽고 있는 책(Assignment) 정보 (DTO로 받음)
        AssignmentResponse currentAssignmentDto = assignmentService.getCurrentAssignment(userId, groupId);

        if (currentAssignmentDto == null) {
            throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
        }

        Long bookId = currentAssignmentDto.getBookId();
        Book currentBook = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found for ID: " + bookId));


        // 현재 활성화된 ReadingSession ID 확보

        List<ReadingSession> currentSessions = readingSessionRepository
                .findByBookAndMemberInAndIsCurrentSession(currentBook, groupMembers, IsCurrentSession.IN_PROGRESS);

        ReadingSession currentSession = currentSessions.stream()
                .filter(session -> session.getMember().getMemberId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Current ReadingSession not found for member: " + memberId));

        Long sessionId = currentSession.getSessionId();

        // ----------------------------------------------------

        // 3. 현재 읽고 있는 책 정보 조회
        CurrentReadingBookResponse readingBookInfo = assignmentService.getCurrentReadingBook(userId, groupId);

        // 4. 책장 흔적 모아보기/전체보기 조회
        // 흔적 모아보기
        List<TraceItemResponse> recentTraceItems = bookShelfService.getNearbyTraces(groupId, bookId, memberId);

        // 흔적 전체보기
        List<TraceGroupResponse> traceGroups = bookShelfService.getAllTracesGrouped(groupId, bookId);

        // 5. 현재 세션의 북마크 목록 조회
        List<BookmarkListResponse> recentBookmarks = bookMarkService.getBookmarksBySessionAndMember(sessionId, memberId);

        // 6. HomeResponse 통합 및 반환
        return HomeResponse.builder()
                .currentGroupId(currentGroup.getGroupId())
                .currentGroupName(currentGroup.getGroupName())
                .groupMemberNicknames(memberNicknames)
                .currentReadingBookInfo(readingBookInfo)
                .traceGroups(traceGroups)
                .recentTraceItems(recentTraceItems)
                .recentBookmarks(recentBookmarks)
                .build();
    }
}