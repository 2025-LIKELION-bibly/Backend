package likelion.bibly.domain.bookshelf.dto;

import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.highlight.entity.Highlight;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record TraceItemResponse(
        // 1. 하이라이트 정보
        Long highlightId,
        String textSentence, // 하이라이트된 텍스트
        String color,
        Integer highlightedPage,

        // 2. 계산된 진행도
        Integer progressPercentage,

        // 3. 메타데이터
        LocalDateTime createdAt,
        Long memberId,

        // 4. 종속된 코멘트 목록
        List<CommentDetailResponse> comments,

        // 5. 블러 여부
        boolean isBlurred // 사용자 진행도보다 높은지 여부 (필수)
) {
    public TraceItemResponse(Highlight highlight, List<Comment> comments, boolean isBlurred, int calculatedProgress) {
        this(
                highlight.getHighlightId(),
                highlight.getTextSentence(),
                highlight.getColor(),
                highlight.getHighlightedPage(),
                calculatedProgress,
                highlight.getCreatedAt(),
                highlight.getMember().getMemberId(),
                comments.stream()
                        .map(CommentDetailResponse::new)
                        .collect(Collectors.toList()),
                isBlurred
        );
    }

    // 코멘트 상세 정보를 위한 내부 DTO
    public record CommentDetailResponse(
            Long commentId,
            String content,
            String visibility,
            LocalDateTime createdAt,
            Long memberId,
            Long parentCommentId
    ) {
        // Comment 엔티티를 받아 DTO로 변환하는 생성자
        public CommentDetailResponse(Comment comment) {
            this(
                    comment.getCommentId(),
                    comment.getContent(),
                    comment.getVisibility().name(),
                    comment.getCreatedAt(),
                    comment.getMember().getMemberId(),
                    comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null
            );
        }
    }
}