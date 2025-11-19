package likelion.bibly.domain.session.service;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.enums.ReadingMode;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ProgressRepository progressRepository;


    /** F.1 최초 진입: 독서 세션 생성 및 초기 상태 설정 */
    @Transactional
    public ReadingSessionResponse startNewReadingSession(Long bookId, Long memberId) {
        // 엔티티 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));

        // 책에 대한 Progress 엔티티 조회/생성
        Progress progress = progressRepository.findByMemberAndBook(member, book)
                .orElseGet(() -> progressRepository.save(Progress.createDefault(member, book)));


        // ReadingSession 엔티티 생성 (F.1 최초 진입 - 집중모드를 디폴트로 진입)
        ReadingSession newSession = ReadingSession.builder()
                .book(book)
                .member(member) // 세션이 모임원에게 귀속
                .progress(progress)
                .mode(ReadingMode.FOCUS) // F.1 최초 진입: 집중모드(FOCUS) 디폴트
                .isCurrentSession(IsCurrentSession.IN_PROGRESS) // 시작하면 즉시 IN_PROGRESS
                .bookMark(0) // 시작은 0 페이지
                .build();

        // DB 저장
        ReadingSession savedSession = readingSessionRepository.save(newSession);

        // 응답 DTO 반환
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
    // TODO: ReadingSession 관련 쓰기 로직 추가

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
    @Transactional
    public ReadingSessionResponse updateBookMark(Long sessionId, Integer pageNumber) {
        // 세션 조회
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        session.updateBookMark(pageNumber);

        // 1. getBook() 또는 getPageCount()가 null일 경우 0을 사용하도록 처리
        Integer pageCount = session.getBook() != null ? session.getBook().getPageCount() : null;
        int totalPages = pageCount != null ? pageCount : 0;

        // 2. totalPages가 0일 경우 NaN 또는 Infinity 방지를 위해 0.0f 할당
        float progressPercent = (totalPages > 0) ? ((float) pageNumber / totalPages) * 100 : 0.0f;
        session.getProgress().updateCurrentPage(pageNumber, progressPercent);

        return new ReadingSessionResponse(session);
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
}