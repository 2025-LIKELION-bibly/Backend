package likelion.bibly.domain.comment.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.comment.enums.AnnotationType;
import likelion.bibly.domain.comment.enums.Visibility;
import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.session.entity.ReadingSession;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession session;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility")
    private Visibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(name = "annotation_type")
    private AnnotationType annotationType;

    // 부모 코멘트 참조 필드 추가
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id", nullable = true) // 최상위 코멘트는 NULL 허용
    private Comment parentComment;

    // 자식 코멘트 리스트 (부모 코멘트에서 모든 답글을 역참조할 때)
    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>();

    @Builder
    public Comment(Highlight highlight, Member member, ReadingSession session, String content, Visibility visibility,
                   AnnotationType annotationType, Comment parentComment) {
        this.highlight = highlight;
        this.member = member;
        this.session = session;
        this.content = content;
        this.visibility = visibility;
        this.annotationType = annotationType;
        this.parentComment = parentComment;
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