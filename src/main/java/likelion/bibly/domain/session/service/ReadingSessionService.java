package likelion.bibly.domain.session.service;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.enums.ReadingMode;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;
    // 엔티티를 찾기 위한 Repository 추가 (가정)
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final ProgressRepository progressRepository;


    /** F.1 최초 진입: 독서 세션 생성 및 초기 상태 설정 */
    @Transactional
    public ReadingSessionResponse startNewReadingSession(String userId, Long bookId, Long memberId) {
        // 엔티티 조회 (실제 코드에서는 Optional 처리 및 예외 던지기 필요)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + userId));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found: " + bookId));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("Member not found: " + memberId));

        // 책에 대한 Progress 엔티티 조회/생성
        // 임시로 Progress가 이미 존재하거나 새로 생성해야 한다고 가정
        Progress progress = progressRepository.findByUserAndBook(user, book)
                .orElseGet(() -> progressRepository.save(Progress.createDefault(user, book)));


        // ReadingSession 엔티티 생성 (F.1 최초 진입 - 집중모드를 디폴트로 진입)
        ReadingSession newSession = ReadingSession.builder()
                .user(user)
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
    public List<ReadingSessionResponse> getOngoingSessionsForUser(String userId) {

        // TODO: "진행 중인" 세션만 가져오는 로직으로 수정 필요
        // **수정 로직**: 특정 사용자 ID와 IN_PROGRESS 상태를 만족하는 세션만 조회
        // (Repository에 findByUser_UserIdAndIsCurrentSession() 메서드가 있다고 가정)
        List<ReadingSession> sessionEntities = readingSessionRepository.findByUser_UserId(userId).stream()
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
    public ReadingSessionResponse changeReadingMode(String sessionId, ReadingMode newMode) {
        // 1. 세션 조회
        // PK 타입 Long 가정: String 타입의 sessionId를 Long으로 변환해야 함 (실제 구현 시 고려)
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        // 2. 모드 업데이트
        session.setMode(newMode);

        // 3. 업데이트된 세션 반환
        return new ReadingSessionResponse(session);
    }

    /** F.2.2 북마크: 현재 진행도를 기준으로 저장 */
    @Transactional
    public ReadingSessionResponse updateBookMark(String sessionId, Integer pageNumber) {
        // 세션 조회
        ReadingSession session = readingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("ReadingSession not found: " + sessionId));

        session.updateBookMark(pageNumber);

        int totalPages = session.getBook().getPageCount();
        float progressPercent = (totalPages > 0) ? ((float) pageNumber / totalPages) * 100 : 0.0f;

        // 4. Progress 엔티티 업데이트
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