package likelion.bibly.domain.group.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 현재 배정받은 책 상태 조회 응답 DTO
 */
@Getter
@Builder
@Schema(description = "현재 배정받은 책 상태 응답")
public class CurrentReadingAssignmentResponse {

	@Schema(description = "모임 ID", example = "1")
	private Long groupId;

	@Schema(description = "모임 이름", example = "즐거운 독서 모임")
	private String groupName;

	@Schema(description = "모임원별 현재 배정 정보")
	private List<MemberCurrentAssignment> memberAssignments;

	@Getter
	@Builder
	@Schema(description = "모임원의 현재 배정 정보")
	public static class MemberCurrentAssignment {

		@Schema(description = "모임원 ID", example = "1")
		private Long memberId;

		@Schema(description = "모임원 닉네임", example = "수진")
		private String nickname;

		@Schema(description = "모임원 색상", example = "RED")
		private String color;

		@Schema(description = "배정 ID", example = "10")
		private Long assignmentId;

		@Schema(description = "현재 읽고 있는 책 ID", example = "5")
		private Long bookId;

		@Schema(description = "현재 읽고 있는 책 제목", example = "Pride and Prejudice")
		private String bookTitle;

		@Schema(description = "현재 읽고 있는 책 표지 이미지 URL")
		private String coverImageUrl;
	}
}