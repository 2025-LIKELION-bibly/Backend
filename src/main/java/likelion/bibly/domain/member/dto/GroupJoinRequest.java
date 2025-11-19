package likelion.bibly.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "모임 참여 요청")
public record GroupJoinRequest(
	@Schema(description = "닉네임", example = "수진2")
	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 1, max = 8, message = "닉네임은 1자에서 8자 사이여야 합니다.")
	String nickname,

	@Schema(description = "색상", example = "BLUE")
	@NotBlank(message = "색상은 필수입니다.")
	String color
) {
}
