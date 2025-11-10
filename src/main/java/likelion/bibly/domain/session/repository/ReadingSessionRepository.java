package likelion.bibly.domain.session.repository;

import likelion.bibly.domain.session.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    // 책읽기 화면: 현재 사용자/도서 기반의 세션 조회 메서드
    List<ReadingSession> findByMember_MemberId(Long memberId);
}