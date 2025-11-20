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

    // Highlight 기본 정보
    private final Long highlightId;
    private final String textSentence; // 하이라이트한 문장
    private final String color;
    private final Integer highlightedPage;
    private final LocalDateTime createdAt;

    // 작성자 정보...엔티티 대신 ID만
    private final Long memberId;

    private final Integer startOffset;
    private final Integer endOffset;

    // Comment 정보
    private final List<CommentResponse> comments;

    // Highlight 엔티티만 인자로 받도록 변경
    public HighlightResponse(Highlight highlight, List<Comment> comments) {
        this.highlightId = highlight.getHighlightId();
        this.textSentence = highlight.getTextSentence();
        this.color = highlight.getColor();
        this.highlightedPage = highlight.getHighlightedPage();
        this.createdAt = highlight.getCreatedAt();

        this.memberId = highlight.getMember().getMemberId();

        //  위치 정보 매핑
        this.startOffset = highlight.getStartOffset();
        this.endOffset = highlight.getEndOffset();

        // Comment 엔티티 리스트를 CommentResponse 리스트로 변환
        this.comments = comments.stream().map(CommentResponse::new).collect(Collectors.toList());
    }
}