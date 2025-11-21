package likelion.bibly.domain.book.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.bibly.domain.book.dto.request.BookSelectRequest;
import likelion.bibly.domain.book.dto.response.BookDetailResponse;
import likelion.bibly.domain.book.dto.response.BookSelectResponse;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.book.service.BookService;
import likelion.bibly.global.auth.AuthUser;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Book", description = "D. 책 고르기 API")
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

	private final BookService bookService;

	/**
	 * D.1.1 새로 나온 책 목록 조회
	 */
	@Operation(
		summary = "새로 나온 책 목록",
		description = """
			최근 등록된 책 목록을 조회합니다.

			**프로세스:**
			1. 데이터베이스에 등록된 책 중 최신순으로 정렬합니다
			2. 상위 20권의 책 목록을 반환합니다

			**반환 정보:**
			- 책 기본 정보 (책 ID, 제목, 저자, 표지 이미지)
			- 최대 20권까지 반환
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = BookSimpleResponse.class))
		)
	})
	@GetMapping("/new")
	public ApiResponse<List<BookSimpleResponse>> getNewBooks() {
		List<BookSimpleResponse> books = bookService.getNewBooks();
		return ApiResponse.success(books);
	}

	/**
	 * D.1.2 지금 많이 읽는 책 (인기 있는 책) 목록 조회
	 */
	@Operation(
		summary = "지금 많이 읽는 책 목록",
		description = """
			전체 사용자들이 많이 읽는 인기 책 목록을 조회합니다.

			**프로세스:**
			1. 책의 인기도 점수를 기준으로 정렬합니다
			2. 상위 20권의 책 목록을 반환합니다

			**인기도 계산 방식:**
			- 교환독서로 선택된 책: +5점

			**반환 정보:**
			- 책 기본 정보 (책 ID, 제목, 저자, 표지 이미지)
			- 최대 20권까지 반환
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = BookSimpleResponse.class))
		)
	})
	@GetMapping("/popular")
	public ApiResponse<List<BookSimpleResponse>> getPopularBooks() {
		List<BookSimpleResponse> books = bookService.getPopularBooks();
		return ApiResponse.success(books);
	}

	/**
	 * D.2.1 책 상세 정보 조회
	 */
	@Operation(
		summary = "책 상세 정보 조회",
		description = """
			책의 상세 정보를 조회합니다.

			**프로세스:**
			1. 책 ID로 책 정보를 조회합니다
			2. 책의 상세 정보를 반환합니다

			**반환 정보:**
			- 책 제목, 저자, 장르
			- 페이지 수, 발행일, 출판사, ISBN
			- 책 소개 (description)
			- 표지 이미지 URL

			**참고:**
			- 선택 가능 여부는 '선택하기' 버튼 클릭 시점에 확인됩니다
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = BookDetailResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "책을 찾을 수 없음 (B001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/{bookId}")
	public ApiResponse<BookDetailResponse> getBookDetail(
		@Parameter(description = "책 ID", example = "1")
		@PathVariable Long bookId
	) {
		BookDetailResponse response = bookService.getBookDetail(bookId);
		return ApiResponse.success(response);
	}

	/**
	 * D.2.2 책 선택 (교환 책으로 선택)
	 */
	@Operation(
		summary = "책 선택",
		description = """
			선택한 책을 교환 책으로 등록합니다.

			**프로세스:**
			1. 책과 모임원 정보를 확인합니다
			2. userId와 groupId로 해당 사용자의 모임원 정보를 조회합니다
			3. 같은 모임의 다른 멤버가 이미 해당 책을 선택했는지 확인합니다
			4. 모임원의 selectedBookId를 업데이트합니다
			5. 책의 인기도 점수를 5점 증가시킵니다
			6. 모든 모임원의 정보와 각자 선택한 책 정보를 반환합니다

			**검증 규칙:**
			- 책이 존재해야 합니다
			- 모임원이 존재해야 합니다
			- 같은 모임의 다른 멤버가 이미 선택하지 않았어야 합니다

			**중복 선택 방지:**
			- 같은 모임의 다른 모임원이 이미 선택한 책은 선택할 수 없습니다
			- 중복 선택 시 409 Conflict 에러 반환
			  → "이미 모임원이 이 책을 골랐어요."

			**인기도 점수:**
			- 교환독서로 선택된 책: +5점

			**반환 정보:**
			- selectedMemberId: 방금 선택한 모임원 ID
			- selectedBookId: 방금 선택한 책 ID
			- selectedBookTitle: 방금 선택한 책 제목
			- message: 완료 메시지
			- members: 모든 모임원 목록
			  - 각 모임원의 닉네임, 색상
			  - 선택한 책 정보 (ID, 제목, 표지 이미지)
			  - 책을 선택하지 않은 모임원도 포함됨 (책 정보는 null)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "책 선택 성공",
			content = @Content(schema = @Schema(implementation = BookSelectResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "책 또는 모임원을 찾을 수 없음 (B001, M001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "이미 모임원이 해당 책을 선택함 (B002)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/{bookId}/select")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<BookSelectResponse> selectBook(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "책 ID", example = "1")
		@PathVariable Long bookId,
		@Valid @RequestBody BookSelectRequest request
	) {
		BookSelectResponse response = bookService.selectBook(bookId, userId, request.groupId());
		return ApiResponse.success(response, "책을 선택했습니다.");
	}

    /**
     *  책 특정 페이지 내용 조회 API
     */
    @GetMapping("/{bookId}/pages/{pageNumber}")
    @Operation(
            summary = "책 특정 페이지 내용 조회",
            description = """
			프론트에서 페이지별로 책 내용을 로드 하기 위한 페이지 내용 조회 API입니다.
			"""
    )
    public ResponseEntity<String> getPageContent(
            @Parameter(description = "책 ID", example = "1")
            @PathVariable Long bookId,
            @PathVariable Integer pageNumber) {

        String content = bookService.getPageContent(bookId, pageNumber);

        return ResponseEntity.ok(content);
    }
}
