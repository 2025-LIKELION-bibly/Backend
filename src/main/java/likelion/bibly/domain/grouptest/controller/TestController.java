package likelion.bibly.domain.grouptest.controller;

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
import likelion.bibly.domain.grouptest.service.TestService;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;

/**
 * 테스트용 컨트롤러
 * 테스트를 위해 모든 환경에서 사용 가능
 */
@Tag(name = "GroupTest", description = "모임 테스트 API")
@RestController
@RequestMapping("/api/v1/test")
@RequiredArgsConstructor
public class TestController {

	private final TestService testService;

	/**
	 * 모든 회차 강제 완료
	 */
	@Operation(
		summary = "[테스트] 모든 회차 강제 완료",
		description = """
			**⚠️ 테스트 전용 API - 테스트하실 때 편의을 위해 만들었습니다**

			현재 회차부터 모임원 수만큼의 모든 회차를 자동으로 생성하고,
			모임 상태를 COMPLETED로 변경합니다.

			**사용 시나리오:**
			- 재시작 기능을 테스트하기 위해 모든 회차를 빠르게 완료
			- 4명의 모임원이 있고 현재 2회차라면 → 3, 4회차를 자동 생성

			**프로세스:**
			1. 현재 회차 확인
			2. 현재 라운드 완료까지 필요한 회차 계산
			3. 부족한 회차만큼 자동으로 배정 생성 (rotateBooks 반복 호출)
			4. 모든 모임원의 책 선택 초기화 (selectedBookId = null)
			5. 모임 상태를 COMPLETED로 변경

			**예시:**
			- 모임원 4명, 현재 회차 2 → 3, 4회차 생성 후 COMPLETED
			- 모임원 3명, 현재 회차 5 → 6회차 생성 후 COMPLETED
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "회차 완료 성공",
			content = @Content(schema = @Schema(implementation = String.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 모임원을 찾을 수 없음 (G001, M001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/groups/{groupId}/complete-all-cycles")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<String> completeAllCycles(
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		String result = testService.completeAllCycles(groupId);
		return ApiResponse.success(result);
	}

	/**
	 * 모임 초기화
	 */
	@Operation(
		summary = "[테스트] 모임 초기화",
		description = """
			**⚠️ 테스트 전용 API - 테스트하실 때 편의을 위해 만들었습니다**

			모임의 모든 배정을 삭제하고 초기 상태로 되돌립니다.

			**주의:**
			- 모든 ReadingAssignment 데이터가 삭제됩니다
			- 한줄평, 독서 기록 등이 모두 사라집니다
			- 복구 불가능합니다
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "초기화 성공",
			content = @Content(schema = @Schema(implementation = String.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임을 찾을 수 없음 (G001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/groups/{groupId}/reset")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<String> resetGroup(
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		String result = testService.resetGroup(groupId);
		return ApiResponse.success(result);
	}
}
