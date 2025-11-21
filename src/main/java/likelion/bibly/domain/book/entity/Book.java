package likelion.bibly.domain.book.entity;
import likelion.bibly.domain.page.entity.PageContent;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<PageContent> pages = new ArrayList<>();

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
        this.popularityScore = 0;
        this.createdAt = LocalDateTime.now();
    }

    public void increasePopularity() {
        this.popularityScore++;
    }

    // pageCount 세터 추가 (계산된 페이지 수 저장용)
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    // PageContent 연결을 위한 Setter
    public void addPage(PageContent page) {
        this.pages.add(page);
        page.setBook(this);
    }
}
