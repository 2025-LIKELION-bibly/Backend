package likelion.bibly.domain.group.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "초대 코드 검증 응답")
@Getter
@Builder
public class InviteCodeValidateResponse {
	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "독서 모임")
	private String groupName;

	@Schema(description = "독서 기간 (일)", example = "14")
	private Integer readingPeriod;

	@Schema(description = "현재 모임원 수", example = "3")
	private Long memberCount;

	@Schema(description = "모임원 정보 리스트")
	private List<MemberSummary> members;

	@Schema(description = "사용 가능한 색상 리스트")
	private List<String> availableColors;

	@Schema(description = "모임원 요약 정보")
	@Getter
	@Builder
	public static class MemberSummary {
		@Schema(description = "닉네임", example = "수진")
		private String nickname;

		@Schema(description = "색상", example = "RED")
		private String color;

		@Schema(description = "책 선택 여부", example = "true")
		private Boolean hasSelectedBook;
	}
}
