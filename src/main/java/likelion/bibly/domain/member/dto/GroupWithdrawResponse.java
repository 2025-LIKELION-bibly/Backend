package likelion.bibly.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "모임 탈퇴 응답")
@Getter
@Builder
public class GroupWithdrawResponse {
	@Schema(description = "탈퇴한 모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "탈퇴한 모임 이름", example = "독서 모임")
	private String groupName;

	@Schema(description = "남은 활성 모임 수", example = "2")
	private Long remainingGroupCount;

	@Schema(description = "완료 메시지", example = "모임에서 탈퇴했습니다.")
	private String message;
}
