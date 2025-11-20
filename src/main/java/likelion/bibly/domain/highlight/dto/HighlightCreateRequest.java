package likelion.bibly.domain.highlight.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "하이라이트 생성 요청 DTO")
public record HighlightCreateRequest(

        @Schema(description = "세션 ID", example = "1")
        Long sessionId,

        @Schema(description = "멤버 ID", example = "42")
        Long memberId,

        @Schema(description = "문장 내용", example = "오늘 엄마가 죽었다. 아니, 어쩌면 어제.")
        String textSentence,

        @Schema(description = "하이라이트 색상", example = "Blue")
        String color,

        @Schema(description = "페이지 번호", example = "150")
        Integer highlightedPage,

        @Schema(description = "페이지 내 시작 오프셋 (문자열 인덱스)", example = "125")
        Integer startOffset,

        @Schema(description = "페이지 내 끝 오프셋 (문자열 인덱스)", example = "180")
        Integer endOffset
) {}