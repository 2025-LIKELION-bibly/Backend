package likelion.bibly.domain.assignment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "다음에 읽을 책 정보 응답")
public class NextReadingBookResponse {

	@Schema(description = "책 ID", example = "2")
	private Long bookId;

	@Schema(description = "책 표지 이미지 URL")
	private String coverImageUrl;

	@Schema(description = "책 제목", example = "To Kill a Mockingbird")
	private String bookTitle;

	@Schema(description = "저자", example = "Harper Lee")
	private String author;

	@Schema(description = "장르", example = "Fiction")
	private String genre;

	@Schema(description = "책 소개", example = "A gripping tale of racial injustice...")
	private String description;

	@Schema(description = "책을 언제부터 읽을 수 있는지", example = "2025-10-31T00:00:00")
	private LocalDateTime availableFrom;

	@Schema(description = "현재 이 책을 읽고 있는 사람의 닉네임", example = "독서왕")
	private String currentReaderNickname;

	@Schema(description = "이 책에 대한 모임원들의 한줄평 목록")
	private List<BookReview> reviews;

	@Getter
	@Builder
	@Schema(description = "책 한줄평 정보")
	public static class BookReview {

		@Schema(description = "모임원 ID", example = "1")
		private Long memberId;

		@Schema(description = "모임원 닉네임", example = "독서왕")
		private String nickname;

		@Schema(description = "모임원 색상", example = "BLUE")
		private String color;

		@Schema(description = "한줄평", example = "정말 감동적인 책이었습니다.")
		private String review;
	}
}
