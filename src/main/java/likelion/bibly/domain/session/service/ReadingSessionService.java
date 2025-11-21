package likelion.bibly.domain.session.service;

import jakarta.persistence.EntityNotFoundException;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
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
    public ReadingSessionResponse finishReadingSession(Long sessionId) {
        // 세션 조회
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("세션을 찾을 수 없습니다."));

        if (session.getIsCurrentSession() == IsCurrentSession.COMPLETED) {
            throw new IllegalStateException("이미 종료된 세션입니다.");
        }

        int totalPages;
        try {
            totalPages = session.getBook().getPageCount();
        } catch (NullPointerException e) {
            // Null이 발생하면 totalPages를 0으로 설정하여 다음 검증 로직으로 넘김
            totalPages = 0;
        }

        // 페이지 수 검증 및 업데이트
        if (session.getBookMark() < totalPages) {
            // 선택: 다 읽지 않았으면 종료 불가 에러 발생
            // throw new IllegalStateException("책의 마지막 페이지에 도달해야 세션을 종료할 수 있습니다.");

            // 선택: 강제로 마지막 페이지로 업데이트 후 종료 (현재 북마크 강제 업데이트)
            session.updateBookMark(totalPages);
        }

        // 상태 변경 (COMPLETED) 및 종료 시간 기록
        session.changeSessionStatus(IsCurrentSession.COMPLETED);
        // session.setFinishedAt(LocalDateTime.now()); // 종료 시간 필드가 있다면

        return  new ReadingSessionResponse(session);
    }


}