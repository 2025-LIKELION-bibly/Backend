package likelion.bibly.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 재시작 가능 여부 응답 DTO
 */
@Getter
@Builder
@Schema(description = "재시작 가능 여부 응답")
public class RestartStatusResponse {

	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "즐거운 독서 모임")
	private String groupName;

	@Schema(description = "현재 회차", example = "8")
	private Integer currentCycle;

	@Schema(description = "총 회차 (활성 멤버 수)", example = "8")
	private Integer totalCycles;

	@Schema(description = "재시작 가능 여부", example = "true")
	private boolean canRestart;

	@Schema(description = "현재 라운드", example = "1")
	private Integer currentRound;

	@Schema(description = "메시지", example = "모든 회차를 완료했습니다. 재시작이 가능합니다.")
	private String message;
}
