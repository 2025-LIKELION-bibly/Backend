package likelion.bibly.domain.progress.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "progress")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "progress")
    private Float progress;

    @Builder
    public Progress(Book book, User user, Integer currentPage, Float progress) {
        this.book = book;
        this.user = user;
        this.currentPage = currentPage;
        this.progress = progress;
        this.lastUpdated = LocalDateTime.now();
    }

    public void updateProgress(Integer currentPage, Float progress) {
        this.currentPage = currentPage;
        this.progress = progress;
        this.lastUpdated = LocalDateTime.now(); // 갱신 시점에 lastUpdated 업데이트
    }
}