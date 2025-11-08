package likelion.bibly.domain.progress.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Member member;

    @Column(name = "current_page")
    private Integer currentPage;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @Column(name = "progress")
    private Float progress;

    @Builder
    public Progress(Book book, Member member, Integer currentPage, Float progress) {
        this.book = book;
        this.member = member;
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