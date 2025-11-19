package likelion.bibly.domain.group.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "모임 생성 요청")
public record GroupCreateRequest(
	@Schema(description = "모임 이름", example = "독서 모임")
	@NotBlank(message = "모임 이름은 필수입니다.")
	@Size(min = 1, max = 15, message = "모임 이름은 1자에서 15자 사이여야 합니다.")
	String groupName,

	@Schema(description = "독서 기간 (일)", example = "14")
	@NotNull(message = "독서 기간은 필수입니다.")
	@Min(value = 7, message = "7일 보다 적게 읽을 수 없어요")
	@Max(value = 60, message = "60일 보다 오래 읽을 수 없어요")
	Integer readingPeriod,

	@Schema(description = "모임장 닉네임", example = "수진")
	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 1, max = 8, message = "닉네임은 1자에서 8자 사이여야 합니다.")
	String nickname,

	@Schema(description = "모임장 색상", example = "RED")
	@NotBlank(message = "색상은 필수입니다.")
	String color
) {
}
