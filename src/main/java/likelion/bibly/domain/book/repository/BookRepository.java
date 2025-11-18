package likelion.bibly.domain.book.repository;

import likelion.bibly.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    // D.1.1 새로 나온 책 (최근 등록순)
    List<Book> findTop20ByOrderByCreatedAtDesc();

    // D.1.2 인기 있는 책 (인기도 높은순)
    List<Book> findTop20ByOrderByPopularityScoreDesc();
}