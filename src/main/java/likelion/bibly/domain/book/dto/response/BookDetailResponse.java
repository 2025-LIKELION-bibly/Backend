package likelion.bibly.domain.book.dto.response;

import java.time.LocalDateTime;

import likelion.bibly.domain.book.entity.Book;
import lombok.Getter;

/**
 * D.2.1 책 상세 정보 응답 DTO
 */
@Getter
public class BookDetailResponse {
    private Long bookId;
    private String title;
    private String author;
    private String genre;
    private Integer pageCount;
    private LocalDateTime publishedAt;
    private String publisher;
    private String isbn;
    private String description;
    private String coverUrl;

    public BookDetailResponse(Book book) {
        this.bookId = book.getBookId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.genre = book.getGenre();
        this.pageCount = book.getPageCount();
        this.publishedAt = book.getPublishedAt();
        this.publisher = book.getPublisher();
        this.isbn = book.getIsbn();
        this.description = book.getDescription();
        this.coverUrl = book.getCoverUrl();
    }
}
