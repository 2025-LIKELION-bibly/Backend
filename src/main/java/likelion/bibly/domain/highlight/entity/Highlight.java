package likelion.bibly.domain.highlight.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.comment.entity.Comment;
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
@Table(name = "highlight")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Highlight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "highlight_id")
    private Long highlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ReadingSession session;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "text_sentence", columnDefinition = "TEXT")
    private String textSentence;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "highlighted_page")
    private Integer highlightedPage;

    @Column(name = "start_offset")
    private Integer startOffset; // 하이라이트 시작 위치 (페이지 내 문자열 인덱스)

    @Column(name = "end_offset")
    private Integer endOffset;   // 하이라이트 끝 위치

    @OneToMany(mappedBy = "highlight", cascade = CascadeType.ALL)
    private List<Comment> comments = new ArrayList<>();

    @Builder
    public Highlight(ReadingSession session, Member member, String textSentence,
                     String color, Integer highlightedPage, Integer startOffset, Integer endOffset) {
        this.session = session;
        this.member = member;
        this.textSentence = textSentence;
        this.color = color;
        this.highlightedPage = highlightedPage;
        this.startOffset = startOffset;
        this.endOffset = endOffset;
        this.createdAt = LocalDateTime.now();
    }

}