package likelion.bibly.domain.progress.repository;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserAndBook(User user, Book book);

}