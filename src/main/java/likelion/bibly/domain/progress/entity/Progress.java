package likelion.bibly.domain.progress.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
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
    @JoinColumn(name = "member_id", nullable = false)
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

    /**
     * ReadingSessionService에서 북마크/페이지 이동 시 호출할 메서드
     * (Service 단에서 퍼센트 계산 후  호출)
     */
    public void updateCurrentPage(Integer currentPage, Float progressPercent) {
        this.currentPage = currentPage;
        this.progress = progressPercent;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * ReadingSessionService에서 새 세션 시작 시 사용할 기본 Progress 생성 팩토리 메서드
     */
    public static Progress createDefault(Member member, Book book) {
        return Progress.builder()
                .member(member)
                .book(book)
                .currentPage(0) // 기본값 0
                .progress(0.0f) // 기본값 0.0
                .build();
    }
}