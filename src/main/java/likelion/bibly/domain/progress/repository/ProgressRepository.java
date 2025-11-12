package likelion.bibly.domain.progress.repository;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.progress.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    // F4 (다시 읽기)
    Optional<Progress> findByBookAndMember(Book book, Member member);
}