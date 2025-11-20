package likelion.bibly.domain.comment.dto;

import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.comment.enums.AnnotationType;
import likelion.bibly.domain.comment.enums.Visibility;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {

    // 기본 Comment 정보
    private final Long commentId;
    private final String content;
    private final Visibility visibility;
    private final LocalDateTime createdAt;

    // 연관 엔티티 ID 정보
    private final Long memberId;
    private final Long readingSessionId;
    private final Long highlightId;

    // 타입 정보 (25자 길이로 결정된 타입)
    private final AnnotationType annotationType;

    // 텍스트 위치 정보 (Highlight 엔티티에서 가져옴)
    private final Integer highlightedPage;
    private final Integer startOffset;
    private final Integer endOffset;

    // 부모 코멘트 ID 추가 (없으면 null)
    private final Long parentCommentId ;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getCommentId();
        this.content = comment.getContent();
        this.visibility = comment.getVisibility();
        this.createdAt = comment.getCreatedAt();

        // 엔티티 관계에서 ID 가져오기
        this.memberId = comment.getMember().getMemberId();
        this.readingSessionId = comment.getSession().getSessionId();
        this.highlightId = comment.getHighlight().getHighlightId();

        // 타입 정보(comment/memo)
        this.annotationType = comment.getAnnotationType();

        // 텍스트 위치 정보 가져오기
        this.highlightedPage = comment.getHighlight().getHighlightedPage();
        this.startOffset = comment.getHighlight().getStartOffset();
        this.endOffset = comment.getHighlight().getEndOffset();

        // 부모 코멘트 ID 매핑
        if (comment.getParentComment() != null) {
            // 답글인 경우 부모 ID 초기화
            this.parentCommentId = comment.getParentComment().getCommentId();
        } else {
            // 최상위 코멘트인 경우 null로 초기화
            this.parentCommentId = null;
        }
    }
}