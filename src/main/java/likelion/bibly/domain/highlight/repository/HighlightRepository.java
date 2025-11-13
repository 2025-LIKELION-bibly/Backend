package likelion.bibly.domain.highlight.repository;

import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, String> {

    // F4 (상세보기)
    List<Highlight> findBySessionAndUser(ReadingSession session, User user);

    // F5 (흔적 모아보기)
    List<Highlight> findBySessionIn(List<ReadingSession> sessions);
}