package likelion.bibly.domain.comment.dto;

import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.comment.enums.Visibility;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponse {
    private Long commentId;
    private String content;
    private Long userID; // 코멘트 작성자
    private Visibility visibility; // 공개 여부
    private LocalDateTime createdAt;

    public CommentResponse(Comment comment) {
        this.commentId = comment.getCommentId();
        this.content = comment.getContent();
        this.userID = getUserID();
        this.visibility = comment.getVisibility();
        this.createdAt = comment.getCreatedAt();
    }
}