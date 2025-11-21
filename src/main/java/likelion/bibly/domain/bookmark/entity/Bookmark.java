package likelion.bibly.domain.bookmark.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookmark")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Bookmark {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bookmark_id")
    private Long bookmarkId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "book_mark_page", nullable = false)
    private Integer bookMarkPage;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Bookmark(ReadingSession session, Member member, Integer bookMarkPage) {
        this.session = session;
        this.member = member;
        this.bookMarkPage = bookMarkPage;
        this.createdAt = LocalDateTime.now();
    }
}