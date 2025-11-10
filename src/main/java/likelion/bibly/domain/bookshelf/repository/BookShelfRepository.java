package likelion.bibly.domain.bookshelf.repository;

import likelion.bibly.domain.bookshelf.entity.BookShelf;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookShelfRepository extends JpaRepository<BookShelf, Long> {
    // 책장 화면 조회용 기본 인터페
    List<BookShelf> findByGroup_GroupId(Long groupId);
}