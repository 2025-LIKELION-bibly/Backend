package likelion.bibly.domain.progress.repository;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, String> {

    // F4 (다시 읽기)
    Optional<Progress> findByBookAndUser(Book book, User user);
}