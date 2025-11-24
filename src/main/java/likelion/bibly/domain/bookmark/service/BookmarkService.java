package likelion.bibly.domain.bookmark.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.bookmark.dto.BookmarkListResponse;
import likelion.bibly.domain.bookmark.dto.BookmarkResponse;
import likelion.bibly.domain.bookmark.entity.Bookmark;
import likelion.bibly.domain.bookmark.repository.BookmarkRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookmarkService {

    private final ReadingSessionRepository readingSessionRepository;
    private final MemberRepository memberRepository;
    private final ProgressRepository progressRepository;
    private final BookmarkRepository bookmarkRepository;

    // --- F.2.2 북마크: 현재 진행도를 기준으로 저장 (단일 북마크/읽던 페이지 저장용) ---
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
            // Progress 엔티티가 없는 경우 예외 발생
            throw new IllegalStateException("Progress entity is missing for session: " + sessionId);
        }
        return ReadingSessionResponse.from(session, pageNumber);
    }

    // --- 북마크 생성용 (사용자가 북마크를 눌러서 추가할 때) ---
    @Transactional
    public BookmarkResponse saveNewBookmark(Long sessionId, Integer currentPageNumber, Long memberId) {
        // 필수 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));
        Book book = session.getBook();

        // Progress 엔티티 조회 또는 생성 (없으면 0%로 자동 생성)
        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() -> progressRepository.save(Progress.createDefault(member, book)));

        // 3. 북마크 엔티티 생성 (이력 관리)
        Bookmark newBookmark = Bookmark.builder()
                .readingsession(session)
                .member(member)
                .bookMarkPage(currentPageNumber)
                .build();

        Bookmark savedBookmark = bookmarkRepository.save(newBookmark);

        // 4. Progress 업데이트 로직 (현재 페이지 번호로 설정)
        Integer pageCount = book != null ? book.getPageCount() : 0;
        float totalPages = (pageCount != null) ? pageCount.floatValue() : 0.0f;
        float progressPercent = (totalPages <= 0) ? 0.0f : (float) currentPageNumber / totalPages;

        progress.updateCurrentPage(currentPageNumber, progressPercent);

        // 5. 응답 DTO 반환
        return new BookmarkResponse(savedBookmark);
    }

    // --- 생성된 북마크 목록 조회 ---
    @Transactional(readOnly = true)
    public List<BookmarkListResponse> getBookmarksBySessionAndMember(Long sessionId, Long memberId) {

        // 1. 엔티티 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        // 2. 레포지토리를 사용하여 북마크 목록 조회 (최신 생성 순)
        List<Bookmark> bookmarkList = bookmarkRepository.findByReadingsessionAndMemberOrderByCreatedAtDesc(session, member);

        // 3. 엔티티 목록을 DTO 목록으로 변환
        List<BookmarkResponse> bookmarkResponses = bookmarkList.stream()
                .map(BookmarkResponse::new)
                .toList();

        // 4. 리스트 DTO로 반환
        return Collections.singletonList(BookmarkListResponse.of(bookmarkResponses));
    }
}