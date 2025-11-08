package likelion.bibly.domain.session.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.progress.entity.Progress;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reading_session")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private Long sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress_id", nullable = false)
    private Progress progress;

    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ReadingMode mode;

    @Column(name = "book_mark")
    private Integer bookMark;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Builder
    public ReadingSession(Member member, Book book, Progress progress, ReadingMode mode, Integer bookMark) {
        this.member = member;
        this.book = book;
        this.progress = progress;
        this.mode = mode;
        this.bookMark = bookMark;
        this.startedAt = LocalDateTime.now(); // 생성 시점에 현재 시간을 시작 일시로 설정
    }


    public void updateBookMark(Integer bookMark) {
        this.bookMark = bookMark;
    }
}