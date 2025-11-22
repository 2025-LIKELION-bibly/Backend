package likelion.bibly.domain.session.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.bookmark.entity.Bookmark;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.session.enums.IsCurrentSession;
import likelion.bibly.domain.session.enums.ReadingMode;
import lombok.*;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "progress", nullable = false)
    private Progress progress;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private ReadingMode mode;

    @Enumerated(EnumType.STRING)
    @Column(name = "is_current_session")
    private IsCurrentSession isCurrentSession;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bookmark_id")
    private Bookmark bookmark;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Builder
    public ReadingSession(Member member, Book book, Group group, Progress progress, ReadingMode mode, IsCurrentSession isCurrentSession, Bookmark bookmark) {
        this.member = member;
        this.book = book;
        this.group = group;
        this.progress = progress;
        this.mode = mode;
        this.isCurrentSession = isCurrentSession;
        this.bookmark = bookmark;
        this.startedAt = LocalDateTime.now(); // 생성 시점에 현재 시간을 시작 일시로 설정
    }


    public void updateBookMark(Integer bookMark) {
    }

    public void changeSessionStatus(IsCurrentSession isCurrentSession) { this.isCurrentSession = isCurrentSession; }

}