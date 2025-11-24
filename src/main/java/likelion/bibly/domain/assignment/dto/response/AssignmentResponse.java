package likelion.bibly.domain.assignment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.book.entity.Book;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 배정 정보 응답 DTO (G.1.2 현재 배정 조회 / G.1.3 다음 배정 조회)
 */
@Getter
@Builder
@Schema(description = "배정 정보 응답")
public class AssignmentResponse {

	@Schema(description = "배정 ID", example = "1")
	private Long assignmentId;

	@Schema(description = "회차", example = "1")
	private Integer cycleNumber;

	@Schema(description = "책 ID", example = "1")
	private Long bookId;

	@Schema(description = "책 제목", example = "The Great Gatsby")
	private String bookTitle;

	@Schema(description = "저자", example = "F. Scott Fitzgerald")
	private String author;

	@Schema(description = "장르", example = "Fiction")
	private String genre;

	@Schema(description = "페이지 수", example = "180")
	private Integer pageCount;

	@Schema(description = "발행일", example = "1925-04-10T00:00:00")
	private LocalDateTime publishedAt;

	@Schema(description = "출판사", example = "Charles Scribner's Sons")
	private String publisher;

	@Schema(description = "ISBN", example = "978-0743273565")
	private String isbn;

	@Schema(description = "책 소개 (줄거리)", example = "A story of decadence and excess...")
	private String description;

	@Schema(description = "표지 이미지 URL")
	private String coverImageUrl;

	@Schema(description = "독서 시작일", example = "2025-01-19T00:00:00")
	private LocalDateTime startDate;

	@Schema(description = "독서 종료일", example = "2025-02-18T23:59:59")
	private LocalDateTime endDate;

	@Schema(description = "한줄평 (작성 전에는 null)", example = "정말 감동적인 책이었습니다.")
	private String review;

	public static AssignmentResponse from(ReadingAssignment assignment) {
		Book book = assignment.getBook();
		return AssignmentResponse.builder()
			.assignmentId(assignment.getAssignmentId())
			.cycleNumber(assignment.getCycleNumber())
			.bookId(book.getBookId())
			.bookTitle(book.getTitle())
			.author(book.getAuthor())
			.genre(book.getGenre())
			.pageCount(book.getPageCount())
			.publishedAt(book.getPublishedAt())
			.publisher(book.getPublisher())
			.isbn(book.getIsbn())
			.description(book.getDescription())
			.coverImageUrl(book.getCoverUrl())
			.startDate(assignment.getStartDate())
			.endDate(assignment.getEndDate())
			.review(assignment.getReview())
			.build();
	}

}
