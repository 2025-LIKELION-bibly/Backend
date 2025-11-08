package likelion.bibly.domain.book.entity;

import jakarta.persistence.*;
        import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "book")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "book_id")
    private Long bookId;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "author", length = 255)
    private String author;

    @Column(name = "genre", length = 255)
    private String genre;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "publisher", length = 100)
    private String publisher;

    @Column(name = "isbn", length = 30)
    private String isbn;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "popularity_score")
    private Integer popularityScore;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public Book(String title, String author, String genre, LocalDateTime publishedAt,
                String publisher, String isbn, Integer pageCount, String description,
                String coverUrl, String content) {
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publishedAt = publishedAt;
        this.publisher = publisher;
        this.isbn = isbn;
        this.pageCount = pageCount;
        this.description = description;
        this.coverUrl = coverUrl;
        this.content = content;
        this.popularityScore = (popularityScore != null) ? popularityScore : 0;
        this.createdAt = LocalDateTime.now();
    }

    public void increasePopularity() {
        this.popularityScore++;
    }
}
