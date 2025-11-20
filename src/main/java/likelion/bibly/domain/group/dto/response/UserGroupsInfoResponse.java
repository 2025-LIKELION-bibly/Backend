package likelion.bibly.domain.group.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 사용자가 속한 모임들의 정보 응답 DTO
 */
@Getter
@Builder
@Schema(description = "사용자가 속한 모임 정보 응답")
public class UserGroupsInfoResponse {

	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private String userId;

	@Schema(description = "속한 모임 목록")
	private List<GroupInfo> groups;

	@Getter
	@Builder
	@Schema(description = "모임 정보")
	public static class GroupInfo {

		@Schema(description = "모임 ID", example = "1")
		private Long groupId;

		@Schema(description = "모임 이름", example = "즐거운 독서 모임")
		private String groupName;

		@Schema(description = "초대 코드 (4자리)", example = "1234")
		private String inviteCode;

		@Schema(description = "모임원 ID (해당 모임에서의 멤버 ID)", example = "10")
		private Long memberId;

		@Schema(description = "모임에서의 닉네임", example = "책벌레")
		private String nickname;

		@Schema(description = "모임에서의 색상", example = "RED")
		private String color;

		@Schema(description = "역할 (LEADER 또는 MEMBER)", example = "LEADER")
		private String role;

		@Schema(description = "모임 상태 (WAITING, IN_PROGRESS, COMPLETED)", example = "WAITING")
		private String groupStatus;
	}
}
