package likelion.bibly.domain.session.repository;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {

    // F1, F2, F3 (책장 상태 구분)
    List<ReadingSession> findByBookAndMemberInAndIsCurrentSession(
            Book book, List<Member> members, IsCurrentSession status
    );

    // F5 (흔적 모아보기)
    List<ReadingSession> findByMemberInAndBookIn(List<Member> members, List<Book> books);

    List<ReadingSession> findByMember_MemberId(Long memberId);
}