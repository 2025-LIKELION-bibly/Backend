package likelion.bibly.domain.member.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Schema(description = "모임 참여 응답")
@Getter
@Builder
public class GroupJoinResponse {
	@Schema(description = "멤버 ID", example = "1")
	private Long memberId;

	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "독서 모임")
	private String groupName;

	@Schema(description = "닉네임", example = "수진2")
	private String nickname;

	@Schema(description = "색상", example = "BLUE")
	private String color;

	@Schema(description = "모임원 정보 리스트")
	private List<MemberInfo> members;

	@Schema(description = "모임원 정보")
	@Getter
	@Builder
	public static class MemberInfo {
		@Schema(description = "닉네임", example = "수진")
		private String nickname;

		@Schema(description = "색상", example = "RED")
		private String color;

		@Schema(description = "선택한 책 ID (없으면 null)", example = "3")
		private Long selectedBookId;

		@Schema(description = "선택한 책 제목 (없으면 null)", example = "Romeo and Juliet")
		private String selectedBookTitle;

		@Schema(description = "책 선택 여부", example = "true")
		private Boolean hasSelectedBook;
	}
}
