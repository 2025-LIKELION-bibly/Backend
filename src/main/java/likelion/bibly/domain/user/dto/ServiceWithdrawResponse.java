package likelion.bibly.domain.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "서비스 탈퇴 응답")
@Getter
@Builder
public class ServiceWithdrawResponse {
	@Schema(description = "탈퇴한 사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private String userId;

	@Schema(description = "완료 메시지", example = "서비스에서 탈퇴했습니다. 그동안 이용해주셔서 감사합니다.")
	private String message;
}
