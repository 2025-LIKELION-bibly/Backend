package likelion.bibly.domain.highlight.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.user.entity.User;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "text_sentence", columnDefinition = "TEXT")
    private String textSentence;

    @Column(name = "color", length = 20)
    private String color;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "highlighted_page")
    private Integer highlightedPage;

    @Builder
    public Highlight(ReadingSession session, User user, String textSentence,
                     String color, Integer highlightedPage) {
        this.session = session;
        this.user = user;
        this.textSentence = textSentence;
        this.color = color;
        this.highlightedPage = highlightedPage;
        this.createdAt = LocalDateTime.now();
    }


    public void updateHighlight(String textSentence, String color) {
        this.textSentence = textSentence;
        this.color = color;
    }
}