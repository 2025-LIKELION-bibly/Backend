package likelion.bibly.domain.assignment.dto.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 책 교환 응답 DTO (G.1.5 책 교환)
 */
@Getter
@Builder
@Schema(description = "책 교환 응답")
public class RotateBooksResponse {

	@Schema(description = "로그인한 사용자의 배정 정보")
	private AssignmentResponse myAssignment;

	@Schema(description = "다른 모임원들의 배정 정보 (memberId + bookId)")
	private List<OtherMemberAssignment> otherMembers;

	@Getter
	@Builder
	@Schema(description = "다른 모임원의 배정 정보")
	public static class OtherMemberAssignment {
		@Schema(description = "모임원 ID", example = "1")
		private Long memberId;

		@Schema(description = "배정받은 책 ID", example = "5")
		private Long bookId;
	}
}
