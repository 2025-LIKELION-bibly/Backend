package likelion.bibly.domain.comment.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.comment.enums.Visibility;
import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private Highlight highlight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    @Builder
    public Comment(Highlight highlight, Member member, String content, Visibility visibility) {
        this.highlight = highlight;
        this.member = member;
        this.content = content;
        this.visibility = visibility;
        this.createdAt = LocalDateTime.now();
    }

    //코멘트 수정
    public void updateContent(String content) {
        this.content = content;
    }

    //공개 여부 수정
    public void updateVisibility(Visibility visibility) {
        this.visibility = visibility;
    }
}