package likelion.bibly.domain.assignment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import likelion.bibly.domain.assignment.dto.request.ReviewWriteRequest;
import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.assignment.dto.response.NextReadingBookResponse;
import likelion.bibly.domain.assignment.service.AssignmentService;
import likelion.bibly.global.auth.AuthUser;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Assignment", description = "G. 교환/재시작 API")
@RestController
@RequestMapping("/api/v1/assignments")
@RequiredArgsConstructor
public class AssignmentController {

	private final AssignmentService assignmentService;

	/**
	 * G.1.1 한줄평 등록
	 */
	@Operation(
		summary = "한줄평 등록",
		description = """
			교환일이 되었을 때 읽은 책에 대한 한줄평을 등록합니다.

			**프로세스:**
			(현재 배정 조회를 활용하여 assignmentid를 얻을 수 있습니다)
			1. 사용자가 최초 진입 시 한줄평 입력 화면 노출
			2. 책 제목과 저자 이름 표시
			3. 사용자가 직접 한줄평 입력 (최대 40자)
			4. 40자 초과 시 "40자까지만 입력할 수 있어요" 안내문구 표시

			**검증 규칙:**
			- 배정 ID 필수
			- 한줄평 최대 40자
			- 본인의 배정에만 한줄평 작성 가능

			**반환 정보:**
			- 업데이트된 배정 정보 (한줄평 포함)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "한줄평 등록 성공",
			content = @Content(schema = @Schema(implementation = AssignmentResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "입력값 검증 실패 - 한줄평 40자 초과 (C001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "403",
			description = "접근 권한 없음 (C005)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "배정을 찾을 수 없음 (A001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PatchMapping("/{assignmentId}/review")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<AssignmentResponse> writeReview(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "배정 ID", example = "1")
		@PathVariable Long assignmentId,
		@Valid @RequestBody ReviewWriteRequest request
	) {
		AssignmentResponse response = assignmentService.writeReview(
			assignmentId,
			userId,
			request.review()
		);
		return ApiResponse.success(response, "한줄평을 등록했습니다.");
	}

	/**
	 * G.1.2 현재 배정 조회
	 */
	@Operation(
		summary = "로그인한 사용자의 현재 배정 조회",
		description = """
			현재 회차에서 로그인한 사용자가 배정받은 책 정보를 조회합니다.

			**프로세스:**
			1. 사용자의 현재 배정된 책 정보 반환
			2. 책 제목, 저자, 표지 등의 정보 포함
			3. 한줄평 작성 유무 확인

			**모든 회차 완료 시:**
			- 마지막 회차의 독서 기간이 경과되면 독서 교환이 끝났으므로 A001 에러 반환
			- 재시작 가능 여부 확인 API로 진행

			**반환 정보:**
			- 배정 ID
			- 회차 (현재 회차)
			- 책 정보 (ID, 제목, 저자, 표지 이미지)
			- 독서 기간 (시작일, 종료일)
			- 한줄평 (작성 전에는 null)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = AssignmentResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임, 모임원, 배정을 찾을 수 없음 또는 모든 회차 완료 (G001, M001, A001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/groups/{groupId}/current-assignment")
	public ApiResponse<AssignmentResponse> getCurrentAssignment(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		AssignmentResponse response = assignmentService.getCurrentAssignment(userId, groupId);
		return ApiResponse.success(response);
	}

	/**
	 * G.1.4 재시작 안내화면
	 */
	@Operation(
		summary = "재시작 전 모임원들의 현재 책 선택 상태",
		description = """
			재시작하기 전에 모임원들의 책 선택 상태를 조회합니다. 
			(읽고 있는 게 아니라 처음에 선택한 책입니다)

			**반환 정보:**
			- 모임 정보 (ID, 이름)
			- 현재 회차
			- 모임원별 정보:
			  - 모임원 ID, 닉네임, 색상
			  - 선택한 책 정보 (ID, 제목, 표지 이미지)
			  - 책을 선택하지 않은 경우 책 정보는 null
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = CurrentAssignmentResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임을 찾을 수 없음 (G001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/groups/{groupId}/current")
	public ApiResponse<CurrentAssignmentResponse> getCurrentAssignments(
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		CurrentAssignmentResponse response = assignmentService.getCurrentAssignments(groupId);
		return ApiResponse.success(response);
	}

	/**
	 * G.1.6 현재 읽고 있는 책 조회
	 */
	@Operation(
		summary = "현재 읽고 있는 책 조회",
		description = """
			현재 로그인된 유저가 현재 모임에서 읽고 있는 책의 정보를 조회합니다.

			**반환 정보:**
			- 책 썸네일 (표지 이미지)
			- 교환독서일까지 남은 기간 (일 수)
			- 다음 교환 독서일 (날짜)
			- 순서대로 읽을 책 표지들 (앞으로 읽을 책들)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = CurrentReadingBookResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임, 모임원, 배정을 찾을 수 없음 (G001, M001, A001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/groups/{groupId}/current-reading")
	public ApiResponse<CurrentReadingBookResponse> getCurrentReadingBook(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		CurrentReadingBookResponse response = assignmentService.getCurrentReadingBook(userId, groupId);
		return ApiResponse.success(response);
	}

	/**
	 * G.1.7 다음에 읽을 책 조회
	 */
	@Operation(
		summary = "다음에 읽을 책 조회",
		description = """
			다음 회차에 읽을 책의 상세 정보를 조회합니다.

			**반환 정보:**
			- 책 표지 이미지
			- 언제부터 읽을 수 있는지 (시작 날짜)
			- 현재 이 책을 읽고 있는 사람의 닉네임
			- 책 정보 (제목, 저자, 장르, 책 소개)
			- 해당 모임 사람들이 남긴 한줄평 전부
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = NextReadingBookResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임, 모임원, 배정을 찾을 수 없음 (G001, M001, A001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/groups/{groupId}/next-reading")
	public ApiResponse<NextReadingBookResponse> getNextReadingBook(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		NextReadingBookResponse response = assignmentService.getNextReadingBook(userId, groupId);
		return ApiResponse.success(response);
	}
}
