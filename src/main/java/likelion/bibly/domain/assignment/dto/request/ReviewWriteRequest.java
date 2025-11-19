package likelion.bibly.domain.assignment.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * G.1.1 한줄평 등록 요청 DTO
 */
public record ReviewWriteRequest(
        @NotNull(message = "한줄평은 필수입니다.")
        @Size(max = 40, message = "40자까지만 입력할 수 있어요.")
        String review
) {
}
