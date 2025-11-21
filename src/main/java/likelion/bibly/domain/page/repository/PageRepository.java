package likelion.bibly.domain.page.repository;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.page.entity.PageContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageContent, Long> {

    /**
     * 특정 책(Book)의 특정 페이지 번호(PageNumber)에 해당하는 내용을 조회
     * @param book 원본 Book 엔티티
     * @param pageNumber 조회할 페이지 순서
     * @return 해당 페이지의 PageContent
     */
    Optional<PageContent> findByBookAndPageNumber(Book book, Integer pageNumber);

    /**
     * 특정 책(Book)에 연결된 모든 페이지 내용을 삭제합니다.
     * 원문 업데이트 등 대비용... 선택사항
     */
    void deleteByBook(Book book);
}