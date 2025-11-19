package likelion.bibly.domain.book.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * D.2.2 책 선택 요청 DTO
 */
public record BookSelectRequest(
        @NotNull(message = "모임 ID는 필수입니다.")
        @Schema(description = "책을 선택하는 모임 ID", example = "1")
        Long groupId
) {
}
