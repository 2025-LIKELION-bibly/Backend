package likelion.bibly.domain.assignment.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 현재 배정 정보 조회 응답 DTO (G.2.1 재시작 안내화면)
 */
@Getter
@Builder
@Schema(description = "현재 배정 정보 응답")
public class CurrentAssignmentResponse {

	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "독서 모임")
	private String groupName;

	@Schema(description = "현재 회차", example = "2")
	private Integer currentCycle;

	@Schema(description = "모임원별 배정 정보")
	private List<MemberAssignmentInfo> memberAssignments;

	@Getter
	@Builder
	@Schema(description = "모임원 배정 정보")
	public static class MemberAssignmentInfo {

		@Schema(description = "모임원 ID", example = "1")
		private Long memberId;

		@Schema(description = "닉네임", example = "수진")
		private String nickname;

		@Schema(description = "색상", example = "RED")
		private String color;

		@Schema(description = "선택한 책이 있는지 여부", example = "true")
		private boolean hasBook;

		@Schema(description = "선택한 책 ID (없으면 null)", example = "5")
		private Long bookId;

		@Schema(description = "선택한 책 제목 (없으면 null)", example = "Pride and Prejudice")
		private String bookTitle;

		@Schema(description = "선택한 책 표지 이미지 URL (없으면 null)")
		private String coverImageUrl;
	}
}