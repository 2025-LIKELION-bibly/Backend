package likelion.bibly.domain.bookshelf.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.bookshelf.dto.*;
import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.comment.repository.CommentRepository;
import likelion.bibly.domain.highlight.dto.HighlightResponse;
import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.highlight.repository.HighlightRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.enums.ReadingMode;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookShelfService {

    private final ReadingAssignmentRepository readingAssignmentRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ProgressRepository progressRepository;
    private final MemberRepository memberRepository;

    // 현재 모든 dto, repository 파일 등은 임시 파일입니다.

    /**
     * F1, F2, F3: 책장 화면 - 특정 그룹의 '진행 중'/'완료' 책장 조회
     */
    public BookShelfResponse getBookshelfByGroup(Long groupId, String currentUserId) {

        List<ReadingAssignment> allAssignments = readingAssignmentRepository.findByGroup_GroupId(groupId);

        List<InProgressBookResponse> inProgressList = new ArrayList<>();
        List<CompletedBookResponse> completedList = new ArrayList<>();

        Set<Book> booksOnShelf = allAssignments.stream()
                .map(ReadingAssignment::getBook)
                .collect(Collectors.toSet());

        // DTO 생성에 사용할 List<BookSimpleResponse>
        List<BookSimpleResponse> bookSimpleList = booksOnShelf.stream()
                .map(BookSimpleResponse::new)
                .collect(Collectors.toList());

        if (allAssignments.isEmpty()) {
            return new BookShelfResponse(
                    bookSimpleList, // 비어있음
                    groupId,
                    null, // comment
                    null, // sessionId
                    currentUserId,
                    inProgressList, // 비어있음
                    completedList // 비어있음
            );
        }

        List<Member> groupMembers = allAssignments.stream()
                .map(ReadingAssignment::getMember)
                .distinct()
                .collect(Collectors.toList());
        LocalDateTime now = LocalDateTime.now();

        for (Book book : booksOnShelf) {
            List<ReadingSession> activeSessions =
                    readingSessionRepository.findByBookAndMemberInAndIsCurrentSession(
                            book, groupMembers, IsCurrentSession.IN_PROGRESS
                    );

            if (!activeSessions.isEmpty()) {
                ReadingSession mainSession = activeSessions.get(0);
                String currentReaderName = null;

                if (mainSession.getMode() == ReadingMode.TOGETHER) {
                    Optional<ReadingAssignment> activeAssignment = allAssignments.stream()
                            .filter(a -> a.getBook().equals(book) &&
                                    a.getStartDate().isBefore(now) &&
                                    a.getEndDate().isAfter(now))
                            .findFirst();

                    if (activeAssignment.isPresent()) {
                        currentReaderName = activeAssignment.get().getMember().getNickname();
                    }
                }

                inProgressList.add(new InProgressBookResponse(
                        book.getTitle(),
                        book.getCoverUrl(),
                        currentReaderName,
                        mainSession.getSessionId()
                ));

            } else {
                List<ReadingSession> completedSessions =
                        readingSessionRepository.findByBookAndMemberInAndIsCurrentSession(
                                book, groupMembers, IsCurrentSession.COMPLETED
                        );

                if (!completedSessions.isEmpty()) {
                    completedList.add(new CompletedBookResponse(
                            book.getTitle(),
                            book.getCoverUrl(),
                            completedSessions.get(0).getSessionId()
                    ));
                }
            }
        }

        return new BookShelfResponse(
                bookSimpleList,   // (List<BookSimpleResponse> books)
                groupId,          // (Long groupId)
                null,             // (String comment)
                null,             // (String sessionId)
                currentUserId,    // (String memberId) - 현재 사용자로 설정
                inProgressList,   // (List<InProgressBookResponse> inProgress)
                completedList     // (List<CompletedBookResponse> completed)
        );
    }

    /**
     * F4: 완료된 책 상세 보기 (책 정보, 북마크, 흔적 보기)
     */
    public CompletedBookDetailResponse getCompletedBookDetails(Long sessionId, Long memberId) {

        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("독서 세션을 찾을 수 없습니다: " + sessionId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + memberId));
        Book book = session.getBook();

        BookInfoResponse bookInfo = new BookInfoResponse(book.getTitle(), book.getAuthor(), book.getBookId(), book.getCoverUrl());
        Integer bookMarkPage = session.getBookMark();

        List<Highlight> myHighlights = highlightRepository.findBySessionAndMember(session, member);
        List<Comment> commentsOnMyHighlights = commentRepository.findByHighlightIn(myHighlights);

        Map<Long, List<Comment>> commentsByHighlightId = commentsOnMyHighlights.stream()
                .collect(Collectors.groupingBy(comment -> comment.getHighlight().getHighlightId()));

        List<HighlightResponse> highlightDtos = myHighlights.stream()
                .map(highlight -> {
                    List<Comment> comments = commentsByHighlightId.getOrDefault(highlight.getHighlightId(), Collections.emptyList());
                    return new HighlightResponse(highlight, comments);
                })
                .collect(Collectors.toList());

        return new CompletedBookDetailResponse(bookInfo, bookMarkPage, highlightDtos);
    }

    /**
     * F4: '다시 읽기' 기능
     */
    @Transactional
    public Long rereadBook(Long completedSessionId, Long memberId) {

        ReadingSession oldSession = readingSessionRepository.findById(completedSessionId)
                .orElseThrow(() -> new EntityNotFoundException("완료된 세션을 찾을 수 없습니다: " + completedSessionId));

        Book book = oldSession.getBook();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다: " + memberId));

        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() ->
                        progressRepository.save(Progress.builder().book(book).member(member).currentPage(0).progress(0f).build())
                );

        ReadingSession newSession = ReadingSession.builder()
                .member(member)
                .book(book)
                .progress(progress)
                .mode(ReadingMode.FOCUS)
                .isCurrentSession(IsCurrentSession.IN_PROGRESS)
                .bookMark(0)
                .build();

        ReadingSession savedSession = readingSessionRepository.save(newSession);
        return savedSession.getSessionId();
    }

    /**
     * F5: 흔적 모아보기 (모임 기준)
     * 모임원 전체의 하이라이트, 코멘트, 메모 조회
     */
    public List<HighlightResponse> getTracesForGroup(Long groupId, Long memberId) {

        // 멤버 유효성 검사 (404)
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 그룹 멤버 전체 조회
        List<Member> groupMembers = memberRepository.findByGroup_GroupId(groupId);

        if (groupMembers.isEmpty()) {
            return Collections.emptyList();
        }

        // 멤버 전체의 세션 조회
        List<ReadingSession> sessions = readingSessionRepository.findByMemberIn(groupMembers);
        if (sessions.isEmpty()) {
            return Collections.emptyList();
        }

        List<Highlight> allHighlights = highlightRepository.findBySessionIn(sessions);
        if (allHighlights.isEmpty()) {
            return Collections.emptyList();
        }

        List<Comment> allComments = commentRepository.findByHighlightIn(allHighlights);

        Map<Long, List<Comment>> commentsByHighlightId = allComments.stream()
                .collect(Collectors.groupingBy(comment -> comment.getHighlight().getHighlightId()));

        // DTO 생성 및 최종 반환
        List<HighlightResponse> highlightDtos = allHighlights.stream()
                .map(highlight -> {
                    // 해당 하이라이트에 연결된 코멘트 리스트
                    List<Comment> comments = commentsByHighlightId.getOrDefault(highlight.getHighlightId(), Collections.emptyList());

                    return new HighlightResponse(highlight, comments);
                })
                .collect(Collectors.toList());

        return highlightDtos;
    }
}