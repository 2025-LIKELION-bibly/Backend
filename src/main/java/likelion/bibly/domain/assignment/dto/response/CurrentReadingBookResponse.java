package likelion.bibly.domain.assignment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "현재 읽고 있는 책 정보 응답")
public class CurrentReadingBookResponse {

	@Schema(description = "책 ID", example = "1")
	private Long bookId;

	@Schema(description = "책 표지 이미지 URL")
	private String coverImageUrl;

	@Schema(description = "교환독서일까지 남은 기간 (일)", example = "30")
	private Long daysRemaining;

	@Schema(description = "다음 교환 독서일", example = "2025-10-31T23:59:59")
	private LocalDateTime nextExchangeDate;

	@Schema(description = "앞으로 읽을 책 목록 (순서대로)")
	private List<UpcomingBook> upcomingBooks;

	@Getter
	@Builder
	@Schema(description = "앞으로 읽을 책 정보")
	public static class UpcomingBook {

		@Schema(description = "책 ID", example = "2")
		private Long bookId;

		@Schema(description = "책 표지 이미지 URL")
		private String coverImageUrl;

		@Schema(description = "회차", example = "2")
		private Integer cycleNumber;
	}
}
