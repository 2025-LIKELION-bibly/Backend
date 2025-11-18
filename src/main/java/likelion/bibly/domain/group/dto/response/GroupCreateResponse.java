package likelion.bibly.domain.group.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "모임 생성 응답")
@Getter
@Builder
public class GroupCreateResponse {
	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "독서 모임")
	private String groupName;

	@Schema(description = "초대 코드", example = "1234")
	private String inviteCode;

	@Schema(description = "독서 기간 (일)", example = "14")
	private Integer readingPeriod;

	@Schema(description = "모임장 멤버 ID", example = "1")
	private Long memberId;
}
