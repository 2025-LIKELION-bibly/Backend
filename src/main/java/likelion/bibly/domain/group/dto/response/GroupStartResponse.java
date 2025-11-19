package likelion.bibly.domain.group.dto.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 교환독서 시작 응답 DTO
 */
@Getter
@Builder
@Schema(description = "교환독서 시작 응답")
public class GroupStartResponse {

	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "즐거운 독서 모임")
	private String groupName;

	@Schema(description = "모임 상태", example = "IN_PROGRESS")
	private String groupStatus;

	@Schema(description = "교환독서 시작 시간", example = "2025-01-19T15:30:00")
	private LocalDateTime startedAt;

	@Schema(description = "독서 기간 (일)", example = "30")
	private Integer readingPeriod;
}
