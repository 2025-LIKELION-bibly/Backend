package likelion.bibly.domain.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.bibly.domain.session.enums.ReadingMode;

@Schema(description = "읽기 모드 변경 요청 DTO")
public record ModeChangeRequest(
        @Schema(description = "변경할 모드", example = "TOGETHER")
        ReadingMode newMode
) {}
