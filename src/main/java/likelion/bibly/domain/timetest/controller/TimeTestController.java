package likelion.bibly.domain.timetest.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.timetest.service.TimeTestService;
import likelion.bibly.global.auth.AuthUser;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "TimeTest", description = "시간 테스트 API (테스트 전용)")
@RestController
@RequestMapping("/api/v1/test/groups/{groupId}/time")
@RequiredArgsConstructor
public class TimeTestController {

	private final TimeTestService timeTestService;

	/**
	 * 시간을 X일 앞으로 점프
	 */
	@Operation(
		summary = "[테스트] 시간 X일 점프",
		description = """
			**⚠️ 테스트 전용 API**
			기능 연동 시 테스트할 때 사용하거나 데모데이 때 시연을 위해 사용하시면 됩니다.
			과하게 남용하면 로직이 꼬여 오류가 발생합니다. 그럴 경우 백엔드에게 연락주세요.

			모든 배정의 시간을 X일 과거로 이동하여 시간이 "점프"한 효과를 냅니다.

			**원리:**
			- 현재 시간은 그대로 유지
			- 모든 ReadingAssignment의 startDate, endDate를 days일 과거로 이동
			- 상대적으로 시간이 앞으로 간 효과

			**예시:**
			- 현재: 2025-11-23
			- 1회차: 2025-11-23 ~ 2025-12-07
			- 2회차: 2025-12-07 ~ 2025-12-21

			14일 점프 후:
			- 1회차: 2025-11-09 ~ 2025-11-23 (완료됨)
			- 2회차: 2025-11-23 ~ 2025-12-07 (현재 회차!)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "시간 점프 성공",
			content = @Content(schema = @Schema(implementation = String.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 배정을 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/forward/{days}")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<String> jumpForward(
		@AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId,
		@Parameter(description = "점프할 일수", example = "14")
		@PathVariable int days
	) {
		String result = timeTestService.jumpForward(groupId, days);
		return ApiResponse.success(result);
	}

	/**
	 * 다음 회차로 점프
	 */
	@Operation(
		summary = "[테스트] 다음 회차로 점프",
		description = """
			**⚠️ 테스트 전용 API**
			기능 연동 시 테스트할 때 사용하거나 데모데이 때 시연을 위해 사용하시면 됩니다.
			과하게 남용하면 로직이 꼬여 오류가 발생합니다. 그럴 경우 백엔드에게 연락주세요.

			readingPeriod일 만큼 시간을 점프하여 다음 회차로 넘어갑니다.

			**사용 예:**
			- 독서 기간이 14일이면 14일 점프
			- 현재 1회차 → 2회차로 바로 이동

			**편의 기능:**
			- forward API에 readingPeriod를 자동으로 전달
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "다음 회차로 점프 성공",
			content = @Content(schema = @Schema(implementation = String.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 배정을 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/next-cycle")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<String> jumpToNextCycle(
		@AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		String result = timeTestService.jumpToNextCycle(groupId);
		return ApiResponse.success(result);
	}

	/**
	 * 재시작 가능 상태로 점프
	 */
	@Operation(
		summary = "[테스트] 재시작 가능 상태로 점프",
		description = """
			**⚠️ 테스트 전용 API**
			기능 연동 시 테스트할 때 사용하거나 데모데이 때 시연을 위해 사용하시면 됩니다.
			과하게 남용하면 로직이 꼬여 오류가 발생합니다. 그럴 경우 백엔드에게 연락주세요.

			모든 회차가 완료된 상태로 점프합니다.

			**자동 계산:**
			- memberCount * readingPeriod + 1일 점프
			- 예: 2명 모임, 14일 독서 기간 → 29일 점프

			**결과:**
			- 모든 회차가 완료됨
			- 재시작 가능 상태 (canRestart = true)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "재시작 가능 상태로 점프 성공",
			content = @Content(schema = @Schema(implementation = String.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 모임원을 찾을 수 없음",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/restart")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<String> jumpToRestart(
		@AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		String result = timeTestService.jumpToRestart(groupId);
		return ApiResponse.success(result);
	}

}
