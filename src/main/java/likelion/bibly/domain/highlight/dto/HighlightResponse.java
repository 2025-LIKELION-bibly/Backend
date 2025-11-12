package likelion.bibly.domain.highlight.dto;
import likelion.bibly.domain.comment.dto.CommentResponse;
import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.highlight.entity.Highlight;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class HighlightResponse {
    // Highlight 정보
    private Long highlightId;
    private String textSentence; // 하이라이트한 문장
    private String color;
    private Integer highlightedPage;
    private Long userId;
    private LocalDateTime createdAt;

    // Comment 정보
    private List<CommentResponse> comments;

    // 생성자: Highlight 엔티티와 그에 달린 Comment 엔티티 리스트
    public HighlightResponse(Highlight highlight, List<Comment> comments) {
        this.highlightId = highlight.getHighlightId();
        this.textSentence = highlight.getTextSentence();
        this.color = highlight.getColor();
        this.highlightedPage = highlight.getHighlightedPage();
        this.userId=getUserId();
        this.createdAt = highlight.getCreatedAt();

        // CommentDto 리스트로 변환
        this.comments = comments.stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }
}