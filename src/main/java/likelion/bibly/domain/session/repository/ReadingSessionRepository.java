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

    List<ReadingSession> findByMember_MemberId(Long memberId);

    List<ReadingSession> findByBookAndMemberInAndIsCurrentSession(Book book, List<Member> groupMembers, IsCurrentSession isCurrentSession);

    List<ReadingSession> findByMemberInAndBookIn(List<Member> groupMembers, List<Book> groupBooks);

    List<ReadingSession> findByMemberIn(List<Member> groupMembers);
}