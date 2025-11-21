package likelion.bibly.domain.bookshelf.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.bookshelf.dto.*;
import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.comment.repository.CommentRepository;
import likelion.bibly.domain.group.entity.Group;
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
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookShelfService {

    private final ReadingAssignmentRepository readingAssignmentRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;
    private final ProgressRepository progressRepository;
    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

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

        Group group = oldSession.getGroup();
        if (group == null) {
            throw new DataIntegrityViolationException("이전 세션에 유효한 그룹 정보가 없어 새 세션을 생성할 수 없습니다.");
        }

        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() ->
                        progressRepository.save(Progress.builder().book(book).member(member).currentPage(0).progress(0f).build())
                );

        ReadingSession newSession = ReadingSession.builder()
                .member(member)
                .book(book)
                .progress(progress)
                .group(group)
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


    /**
     * Helper: highlightedPage와 Book의 pageCount를 이용하여 진행도(%)를 계산
     */
    private int calculateProgressPercentage(int highlightedPage, int totalPageCount) {
        if (totalPageCount <= 0 || highlightedPage <= 0) return 0;

        // (현재 페이지 / 총 페이지 수) * 100 계산
        double progress = ((double) highlightedPage / totalPageCount) * 100;

        // 소수점 반올림 후 100%를 초과하지 않도록 보장
        return (int) Math.min(100, Math.round(progress));
    }

    // E.1.7 흔적 모아보기: 사용자의 진행도 (+-10%) 근처 흔적 조회
    public List<TraceItemResponse> getNearbyTraces(Long groupId, Long bookId, Long memberId) {

        // 1. 책 정보 조회 및 현재 진행도 조회
        memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 총 페이지 수(pageCount) 확보
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId));
        final int totalPageCount = book.getPageCount();

        Progress memberProgressEntity = progressRepository.findByMember_MemberIdAndBook_BookId(memberId, bookId)
                .orElseThrow(() -> new EntityNotFoundException("멤버(" + memberId + ")의 책(" + bookId + ") 진행도를 찾을 수 없습니다."));

        final int memberProgress = memberProgressEntity.getCurrentProgressPercentage();
        final int minProgress = Math.max(0, memberProgress - 10);
        final int maxProgress = Math.min(100, memberProgress + 10);

        // 2. 해당 책과 그룹의 모든 하이라이트/코멘트 조회
        List<Highlight> allHighlights = highlightRepository
                .findBySession_Book_BookIdAndSession_Group_GroupId(bookId, groupId);

        List<Comment> allComments = commentRepository
                .findCommentsByBookIdAndGroupIdUsingJoins(bookId, groupId);

        if (allHighlights.isEmpty() && allComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 코멘트 맵핑 (하이라이트 ID 기준)
        Map<Long, List<Comment>> commentsByHighlightId = allComments.stream()
                .filter(comment -> comment.getHighlight() != null)
                .collect(Collectors.groupingBy(comment -> comment.getHighlight().getHighlightId()));

        // 4. TraceItemResponse DTO 생성 및 필터링
        List<TraceItemResponse> nearbyTraces = allHighlights.stream()
                .map(highlight -> {
                    // 진행도 계산
                    int calculatedProgress = calculateProgressPercentage(highlight.getHighlightedPage(), totalPageCount);

                    List<Comment> comments = commentsByHighlightId.getOrDefault(highlight.getHighlightId(), Collections.emptyList());

                    boolean isBlurred = calculatedProgress > memberProgress;

                    return new TraceItemResponse(highlight, comments, isBlurred, calculatedProgress);
                })
                .filter(traceItem -> {
                    int progress = traceItem.progressPercentage();
                    return progress >= minProgress && progress <= maxProgress;
                })
                .sorted(Comparator.comparing(TraceItemResponse::progressPercentage)) // 계산된 진행도 오름차순 정렬
                .collect(Collectors.toList());

        return nearbyTraces;
    }

    /**
     * E.1.8 흔적 전체보기: 10% 단위 그룹화 (bookId 필요)
     */
    public List<TraceGroupResponse> getAllTracesGrouped(Long groupId, Long bookId) {

        // 1. 책 정보 조회 (총 페이지 수 확보)
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId));
        final int totalPageCount = book.getPageCount(); // 총 페이지 수 확보

        // 2. 해당 책과 그룹의 모든 하이라이트/코멘트 조회
        List<Highlight> allHighlights = highlightRepository
                .findBySession_Book_BookIdAndSession_Group_GroupId(bookId, groupId);

        List<Comment> allComments = commentRepository
                .findCommentsByBookIdAndGroupIdUsingJoins(bookId, groupId);

        if (allHighlights.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 코멘트 맵핑
        Map<Long, List<Comment>> commentsByHighlightId = allComments.stream()
                .filter(comment -> comment.getHighlight() != null)
                .collect(Collectors.groupingBy(comment -> comment.getHighlight().getHighlightId()));

        // 4. TraceItemResponse DTO로 통합 및 정렬
        List<TraceItemResponse> allTraces = allHighlights.stream()
                .map(highlight -> {
                    // 진행도 계산
                    int calculatedProgress = calculateProgressPercentage(highlight.getHighlightedPage(), totalPageCount);

                    List<Comment> comments = commentsByHighlightId.getOrDefault(highlight.getHighlightId(), Collections.emptyList());
                    // 계산된 진행도를 DTO에 전달 (isBlurred는 false)
                    return new TraceItemResponse(highlight, comments, false, calculatedProgress);
                })
                .sorted(Comparator.comparing(TraceItemResponse::progressPercentage))
                .collect(Collectors.toList());

        // 5. 10% 단위로 그룹화
        Map<Integer, List<TraceItemResponse>> groupedByRange = allTraces.stream()
                .collect(Collectors.groupingBy(
                        trace -> (trace.progressPercentage() / 10) * 10
                ));

        // 6. 0%부터 90%까지의 모든 구간에 대해 TraceGroupResponse DTO 생성
        return IntStream.rangeClosed(0, 90)
                .filter(i -> i % 10 == 0)
                .mapToObj(start -> {
                    String range = String.format("%d%% ~ %d%%", start, start + 10);
                    List<TraceItemResponse> rangeTraces = groupedByRange.getOrDefault(start, Collections.emptyList());

                    return new TraceGroupResponse(range, rangeTraces.size(), rangeTraces);
                })
                .collect(Collectors.toList());
    }
}