package likelion.bibly.domain.highlight.repository;

import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.session.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightRepository extends JpaRepository<Highlight, Long> {

    // F4 (상세보기)
    List<Highlight> findBySessionAndMember(ReadingSession session, Member member);

    // F5 (흔적 모아보기)
    List<Highlight> findBySessionIn(List<ReadingSession> sessions);
}