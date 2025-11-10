package likelion.bibly.domain.session.service;

import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;

    /** 책읽기 화면: 현재 진행 중인 독서 세션 조회 */
    public List<ReadingSession> getOngoingSessionsForMember(Long memberId) {
        // TODO: 사용자의 독서 세션 조회하는 비즈니스 로직 구현
        return readingSessionRepository.findByMember_MemberId(memberId);
    }

    // TODO: ReadingSession 관련 쓰기 로직 추가
}