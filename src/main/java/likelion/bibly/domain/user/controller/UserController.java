package likelion.bibly.domain.user.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import likelion.bibly.domain.group.dto.response.UserGroupsInfoResponse;
import likelion.bibly.domain.user.dto.response.ServiceWithdrawResponse;
import likelion.bibly.domain.user.dto.response.UserCreateResponse;
import likelion.bibly.domain.user.service.UserService;
import likelion.bibly.global.auth.AuthUser;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "User", description = "사용자 관리 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@Operation(
		summary = "사용자 생성",
		description = """
            새로운 UUID 기반 사용자를 생성합니다.

            **로컬스토리지 사용 방법:**
            1. API 호출
            2. 반환된 userId를 localStorage에 저장
            3. 이후 모든 API 요청 시 헤더에 "X-User-Id: {userId}" 포함
            """
	)
	@PostMapping
	public ResponseEntity<UserCreateResponse> createUser() {
		UserCreateResponse response = userService.createUser();
		return ResponseEntity.ok(response);
	}

	/**
	 * 사용자가 속한 모임 정보 조회 (E.2.3 다른 모임으로 이동하기)
	 */
	@Operation(
		summary = "사용자 정보 조회",
		description = """
			현재 로그인 한 사용자가 속한 모든 모임의 정보와 uuid를 조회합니다.
			편의를 위해 만든 API입니다.

			**프로세스:**
			1. 사용자 ID로 속한 모든 활성 모임을 조회합니다
			2. 각 모임에서의 멤버 정보를 함께 반환합니다

			**반환 정보:**
			- 사용자 ID
			- 속한 모임 목록:
			  - 모임 ID, 모임 이름
			  - 해당 모임에서의 멤버 ID, 닉네임, 색상, 역할
			  - 모임 상태 (WAITING, IN_PROGRESS, COMPLETED)

			**사용 사례:**
			- E.2.3 다른 모임으로 이동하기 기능
			- 모임 목록 화면
			- 좌우 버튼으로 다른 모임 화면 전환
			  (다른 모임이 없을 경우 버튼 표시 안 함)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = UserGroupsInfoResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "사용자를 찾을 수 없음 (U001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/groups")
	public ApiResponse<UserGroupsInfoResponse> getMyGroups(
		@Parameter(hidden = true) @AuthUser String userId
	) {
		UserGroupsInfoResponse response = userService.getUserGroupsInfo(userId);
		return ApiResponse.success(response);
	}

	/**
	 * 서비스 탈퇴 (A.3.3 서비스 탈퇴 → A.3.4 서비스 탈퇴 완료)
	 */
	@Operation(
		summary = "서비스 탈퇴",
		description = """
			사용자를 서비스에서 탈퇴 처리합니다.

			**프로세스:**
			1. 사용자의 모든 모임에서 자동으로 탈퇴 처리됩니다
			2. 탈퇴한 모임에서 닉네임은 "탈퇴한 모임원"으로 변경됩니다
			3. 색상은 회색(GRAY)으로 변경됩니다
			4. 사용자 상태가 WITHDRAWN으로 변경됩니다

			**UI 처리:**
			- 탈퇴 전 안내문구 표시
			- 사용자가 안내사항을 확인했다는 체크박스를 터치해야 탈퇴 가능

			**탈퇴 후 영향:**
			- 교환독서가 진행 중인 경우에도 탈퇴 가능
			- 탈퇴 후에도 다른 모임원들은 정상적으로 교환독서 진행
			- 탈퇴한 사용자가 선택한 책을 읽고 있던 모임원이 있다면, 해당 책은 읽는 사람이 없는 상태가 될 수 있음
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "서비스 탈퇴 성공",
			content = @Content(schema = @Schema(implementation = ServiceWithdrawResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "이미 탈퇴한 사용자 (U002)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "사용자를 찾을 수 없음 (U001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@DeleteMapping("/withdraw")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<ServiceWithdrawResponse> withdrawFromService(
		@Parameter(hidden = true) @AuthUser String userId
	) {
		ServiceWithdrawResponse response = userService.withdrawFromService(userId);
		return ApiResponse.success(response);
	}
}
