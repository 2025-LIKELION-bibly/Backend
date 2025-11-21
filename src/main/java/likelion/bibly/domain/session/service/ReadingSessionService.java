package likelion.bibly.domain.session.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.bookmark.dto.BookmarkListResponse;
import likelion.bibly.domain.bookmark.dto.BookmarkResponse;
import likelion.bibly.domain.bookmark.entity.Bookmark;
import likelion.bibly.domain.bookmark.repository.BookmarkRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.enums.ReadingMode;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ProgressRepository progressRepository;
    private final GroupRepository groupRepository;
    private final BookmarkRepository bookmarkRepository;
    private final ReadingAssignmentRepository readingAssignmentRepository;

    /** F.1 최초 진입: 독서 세션 생성 및 초기 상태 설정 */
    @Transactional
    public ReadingSessionResponse startNewReadingSession(Long bookId, Long memberId, Long groupId) {
        // 1. 엔티티 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Group not found: " + groupId));

        // 2. 책에 대한 Progress 엔티티 조회/생성
        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() -> progressRepository.save(Progress.createDefault(member, book)));


        // 3. ReadingSession 엔티티 생성
        ReadingSession newSession = ReadingSession.builder()
                .book(book)
                .group(group)
                .member(member)
                .progress(progress)
                .mode(ReadingMode.FOCUS)
                .isCurrentSession(IsCurrentSession.IN_PROGRESS)
                .bookMark(0)
                .build();

        // 4. DB 저장
        ReadingSession savedSession = readingSessionRepository.save(newSession);

        // 5. 응답 DTO 반환
        return new ReadingSessionResponse(savedSession);
    }


    /** 책읽기 화면: 현재 진행 중인 독서 세션 조회 (F.2 화면을 탭했을 때 정보 표시) */
    @Transactional(readOnly = true)
    public List<ReadingSessionResponse> getOngoingSessionsForMember(Long memberId) {

        List<ReadingSession> sessionEntities = readingSessionRepository.findByMember_MemberId(memberId).stream()
                .filter(session -> session.getIsCurrentSession() == IsCurrentSession.IN_PROGRESS)
                .toList();

        List<ReadingSessionResponse> sessionDtos = sessionEntities.stream()
                .map(ReadingSessionResponse::new)
                .collect(Collectors.toList());

        return sessionDtos;
    }

    // ----------------------------------------------------------------------------------

    /** F.1 토글: 모드 전환 (집중 <-> 같이) */
    @Transactional
    public ReadingSessionResponse changeReadingMode(Long sessionId, ReadingMode newMode) {
        // 1. 세션 조회
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        // 2. 모드 업데이트
        session.setMode(newMode);

        // 3. 업데이트된 세션 반환
        return new ReadingSessionResponse(session);
    }

    /** F.2.2 북마크: 현재 진행도를 기준으로 저장 */

    // 단일 북마크(읽던 페이지, 현재 페이지 저장용)
    @Transactional
    public ReadingSessionResponse updateBookMark(Long sessionId, Long memberId, Integer pageNumber) throws AccessDeniedException {

        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        // 멤버 유효성 검사 추가
        if (!session.getMember().getMemberId().equals(memberId)) {
            throw new AccessDeniedException("해당 세션(" + sessionId + ")에 멤버(" + memberId + ")가 소속되지 않습니다.");
        }

        session.updateBookMark(pageNumber);

        Progress progress = session.getProgress();
        Book book = session.getBook();

        // 페이지 수 확보
        Integer pageCount = (book != null) ? book.getPageCount() : null;
        int totalPages = (pageCount != null) ? pageCount : 0;

        // 진행도 퍼센트 계산
        float progressPercent = (totalPages > 0) ? ((float) pageNumber / totalPages) : 0.0f;

        // Progress 업데이트
        if (progress != null) {
            progress.updateCurrentPage(pageNumber, progressPercent);
        } else {
            // Progress 엔티티가 없는 경우 예외 발생 또는 로깅
            throw new IllegalStateException("Progress entity is missing for session: " + sessionId);
        }

        return new ReadingSessionResponse(session);
    }

    // 북마크 생성용(사용자가 북마크를 눌러서 추가할 때)
    @Transactional
    public BookmarkResponse saveNewBookmark(Long sessionId, Integer currentPageNumber, Long memberId) {
        // 필수 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));
        Book book = session.getBook();

        //  Progress 엔티티 조회 또는 생성 (없으면 0%로 자동 생성)
        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() -> progressRepository.save(Progress.createDefault(member, book)));

        // 3. 북마크 엔티티 생성 (이력 관리)
        Bookmark newBookmark = Bookmark.builder()
                .session(session)
                .member(member)
                .bookMarkPage(currentPageNumber)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);

        // 4. Progress 업데이트 로직 (현재 페이지 번호로 설정)
        float totalPages = book.getPageCount();
        float progressPercent = (totalPages <= 0) ? 0.0f : (float) currentPageNumber / totalPages;

        progress.updateCurrentPage(currentPageNumber, progressPercent);

        // 5. 응답 DTO 반환
        return new BookmarkResponse(savedBookmark);
    }

    // 생성된 북마크 목록 조회
    @Transactional(readOnly = true)
    public BookmarkListResponse getBookmarksBySessionAndMember(Long sessionId, Long memberId) {

        // 1. 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        // 2. 레포지토리를 사용하여 북마크 목록 조회 (최신 생성 순)
        List<Bookmark> bookmarkList = bookmarkRepository.findBySessionAndMemberOrderByCreatedAtDesc(session, member);

        // 3. 엔티티 목록을 DTO 목록으로 변환
        List<BookmarkResponse> bookmarkResponses = bookmarkList.stream()
                .map(BookmarkResponse::new)
                .toList();

        // 4. 리스트 DTO로 반환
        return BookmarkListResponse.of(bookmarkResponses);
    }

    // ----------------------------------------------------------------------------------
    // F.4 같이모드: 다른 모임원들의 형광펜, 코멘트 등을 조회
    @Transactional(readOnly = true)
    public List<ReadingSessionResponse> getOthersActiveSessionsInGroup(Book book, List<Member> groupMembers) {

        List<ReadingSession> othersSessions = readingSessionRepository.findByBookAndMemberInAndIsCurrentSession(
                book,
                groupMembers,
                IsCurrentSession.IN_PROGRESS // 진행 중인 세션만 조회
        );

        return othersSessions.stream()
                .map(ReadingSessionResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public ReadingSessionResponse finishReadingSession(Long sessionId, Long memberId) throws BusinessException {
        // 세션 조회
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_NOT_FOUND));

        // 이미 종료된 상태인지 확인
        if (session.getIsCurrentSession() == IsCurrentSession.COMPLETED) {
            throw new BusinessException(ErrorCode.SESSION_ALREADY_COMPLETED);
        }

        // 세션의 멤버 ID와 요청한 멤버 ID가 일치하는지 확인
        if (!session.getMember().getMemberId().equals(memberId)) {
            throw new BusinessException(ErrorCode.SESSION_DELETE_ACCESS_DENIED);
        }

        Group group = session.getGroup();
        if (group == null) {
            // 그룹이 없는 세션은 기간 만료 체크 없이 수동 종료만 가능하도록 처리(필요 시)
            session.changeSessionStatus(IsCurrentSession.COMPLETED);
            return new ReadingSessionResponse(session);
        }

        // 해당 그룹의 할당 정보 조회
        ReadingAssignment assignment = readingAssignmentRepository.findTopByGroup_GroupIdOrderByCreatedAtDesc(group.getGroupId())
                .orElseThrow(() -> new BusinessException(ErrorCode.SESSION_ASSIGNMENT_NOT_FOUND));

        // 기간 만료 체크 (자동 종료 로직)
        LocalDate today = LocalDate.now();
        LocalDate endDate = assignment.getEndDate().toLocalDate();

        // 현재 날짜가 마감일을 지났다면 (기간 만료)
        if (today.isAfter(endDate)) {
            session.changeSessionStatus(IsCurrentSession.COMPLETED);

            System.out.println("자동 종료: 독서 할당 기간 만료로 세션이 종료되었습니다. Session ID: " + sessionId);
        }
        else {
            // 수동 종료 (기간 만료 전)
            session.changeSessionStatus(IsCurrentSession.COMPLETED);
        }

        return new ReadingSessionResponse(session);
    }


}