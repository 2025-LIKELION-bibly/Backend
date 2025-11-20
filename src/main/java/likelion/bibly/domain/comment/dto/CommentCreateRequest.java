package likelion.bibly.domain.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.bibly.domain.comment.enums.Visibility;

@Schema(description = "코멘트/메모 생성 요청 DTO")
public record CommentCreateRequest(

        @Schema(description = "연결할 하이라이트 ID", example = "5")
        Long highlightId,

        @Schema(description = "멤버 ID", example = "42")
        Long memberId,

        @Schema(description = "코멘트/메모 내용 (25자 초과 시 메모로 자동 분류)",
                example = "알베르 카뮈 이방인의 명문장")
        String content,

        @Schema(description = "공개 여부", example = "PUBLIC")
        Visibility visibility,

        @Schema(description = "부모 코멘트 ID (최상위 코멘트인 경우 null)", example = "10")
        Long parentCommentId // 부모 코멘트 ID (답글이 아닌 경우 null)
) {}