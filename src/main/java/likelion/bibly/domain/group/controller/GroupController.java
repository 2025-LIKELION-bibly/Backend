package likelion.bibly.domain.group.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import likelion.bibly.domain.group.dto.request.GroupCreateRequest;
import likelion.bibly.domain.group.dto.response.GroupCreateResponse;
import likelion.bibly.domain.group.dto.response.GroupStartResponse;
import likelion.bibly.domain.group.dto.response.InviteCodeValidateResponse;
import likelion.bibly.domain.group.dto.response.GroupMembersBookResponse;
import likelion.bibly.domain.group.dto.response.RestartStatusResponse;
import likelion.bibly.domain.group.service.GroupService;
import likelion.bibly.domain.member.dto.GroupJoinRequest;
import likelion.bibly.domain.member.dto.GroupJoinResponse;
import likelion.bibly.domain.member.dto.GroupWithdrawResponse;
import likelion.bibly.domain.member.service.MemberService;
import likelion.bibly.global.auth.AuthUser;
import likelion.bibly.global.common.ApiResponse;
import likelion.bibly.global.exception.ErrorResponse;
import lombok.RequiredArgsConstructor;

@Tag(name = "Group", description = "C. 모임 관리 API")
@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

	private final GroupService groupService;
	private final MemberService memberService;

	/**
	 * 모임 생성 (C.2 모임 생성)
	 */
	@Operation(
		summary = "모임 생성",
		description = """
			새로운 독서 모임을 생성합니다.

			**프로세스:**
			1. 모임 이름과 독서 기간을 설정합니다
			2. 4자리 숫자 초대 코드가 자동으로 생성됩니다
			3. 생성자는 자동으로 모임장(LEADER) 역할을 부여받습니다
			4. 모임 상태는 WAITING으로 시작됩니다

			**검증 규칙:**
			- 모임 이름: 1~15자 (띄어쓰기 포함 가능)
			  - 0자거나 15자를 넘길 시 다음으로 넘어가지 못함
			  - 16자 이상 입력 시 "모임 이름은 15자까지만 입력할 수 있어요" 문구 노출
			- 독서 기간: 7~60일
			  - 7일 미만 입력 시 "7일 보다 적게 읽을 수 없어요" 문구 노출
			  - 60일 초과 입력 시 "60일 보다 오래 읽을 수 없어요" 문구 노출
			- 닉네임: 1~8자, 영문/한글/숫자만 허용 (특수문자 불가)
			  - 8자 초과 시 "닉네임은 8자까지만 입력할 수 있어요" 문구 노출
			  - 특수문자 포함 시 "특수문자는 사용할 수 없어요" 문구 노출
			- 색상: RED, BLUE, GREEN, YELLOW, PURPLE, ORANGE, PINK, CYAN 중 하나
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "모임 생성 성공",
			content = @Content(schema = @Schema(implementation = GroupCreateResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "입력값 검증 실패 (C001, M004, M005)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "사용자를 찾을 수 없음 (U001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "500",
			description = "서버 오류 (C002)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<GroupCreateResponse> createGroup(
		@Parameter(hidden = true) @AuthUser String userId,
		@Valid @RequestBody GroupCreateRequest request
	) {
		GroupCreateResponse response = groupService.createGroup(userId, request);
		return ApiResponse.success(response, "모임이 생성되었습니다.");
	}

	/**
	 * 초대 코드 검증 (C.3.1 코드 입력 → C.3.1.1 입력 확인)
	 */
	@Operation(
		summary = "초대 코드 검증",
		description = """
			4자리 숫자 초대 코드로 모임 정보를 조회합니다.

			**프로세스:**
			1. 초대 코드를 입력받아 모임을 조회합니다
			2. 모임원이 8명으로 가득 찼는지 확인합니다
			3. 모임 정보와 현재 모임원 목록을 반환합니다
			4. 사용 가능한 색상 목록을 함께 반환합니다

			**입력 규칙:**
			- 4자리의 숫자만 입력 가능
			- 입력창 클릭 시 숫자패드 노출
			- 숫자가 4자리 모두 입력되어야 다음 버튼 활성화

			**반환 정보:**
			- 모임 기본 정보 (이름, 독서 기간, 현재 인원)
			- 모임원 목록 (닉네임, 색상, 책 선택 여부)
			- 사용 가능한 색상 목록 (중복 제외)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "초대 코드 검증 성공",
			content = @Content(schema = @Schema(implementation = InviteCodeValidateResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "유효하지 않은 코드 또는 모임원 가득 참 (G002, G004)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/invite/{inviteCode}")
	public ApiResponse<InviteCodeValidateResponse> validateInviteCode(
		@Parameter(description = "초대 코드 (4자리 숫자)", example = "1234")
		@PathVariable String inviteCode
	) {
		InviteCodeValidateResponse response = groupService.validateInviteCode(inviteCode);
		return ApiResponse.success(response);
	}

	/**
	 * 모임 참여 (C.3.2 사용자 닉네임 설정 → C.3.4 모임 참여 완료)
	 */
	@Operation(
		summary = "모임 참여",
		description = """
			초대 코드로 검증한 모임에 참여합니다.
			(권장: 초대 코드 검증 API를 먼저 호출하여 사용 가능한 색상 목록을 확인하면 더 나은 UX를 제공할 수 있습니다)

			**프로세스:**
			1. 사용자 정보를 확인합니다
			2. 모임 존재 여부를 확인합니다
			3. 닉네임과 색상의 중복을 확인합니다
			4. 모임원으로 등록합니다 (역할: MEMBER)
			5. 참여 완료 화면에 표시할 모임원 정보를 반환합니다

			**검증 규칙 (C.3.2 닉네임 설정):**
			- 닉네임: 1~8자, 영문/한글/숫자만 허용 (특수문자 불가)
			  - 최대 8자까지 입력 가능
			  - 입력값 기준 충족 시 '다음으로' 버튼 활성화
			  - 오류 발생 시 버튼 비활성화 유지
			- 색상: 8가지 색상 중 하나, 모임 내 고유
			  - 사용 가능한 색상 리스트 노출 및 해당 색 선택 불가능 처리
			- 모임원 수: 최대 8명

			**반환 정보 (C.3.4 모임 참여 완료):**
			- 참여한 멤버 정보 (memberId, nickname, color)
			- 모임 정보 (groupId, groupName)
			- 전체 모임원 목록 (닉네임, 색상, 선택한 책 정보)
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "201",
			description = "모임 참여 성공",
			content = @Content(schema = @Schema(implementation = GroupJoinResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "입력값 검증 실패 또는 모임원 가득 참 (C001, G004, M004, M005)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "사용자 또는 모임을 찾을 수 없음 (U001, G001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "409",
			description = "중복 가입 또는 닉네임/색상 중복 (U003, M002, M003)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/{groupId}/members")
	@ResponseStatus(HttpStatus.CREATED)
	public ApiResponse<GroupJoinResponse> joinGroup(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId,
		@Valid @RequestBody GroupJoinRequest request
	) {
		GroupJoinResponse response = groupService.joinGroup(userId, groupId, request);
		return ApiResponse.success(response, "모임에 참여했습니다.");
	}

	/**
	 * 모임 정보 조회 (모든 모임원 + 각자 선택한 책)
	 */
	@Operation(
		summary = "모임 정보 조회",
		description = """
			현재 모임의 모든 모임원과 각자 선택한 책 정보를 조회합니다.
			다른 리턴 값에 필요한 내용은 다 넣긴 했는데 혹시 몰라 해당 api도 만들어 두었습니다.

			**프로세스:**
			1. 모임 정보를 조회합니다
			2. 해당 모임의 모든 활성 멤버를 조회합니다
			3. 각 모임원이 선택한 책 정보를 함께 반환합니다

			**반환 정보:**
			- 모임 기본 정보 (ID, 이름)
			- 모든 모임원 목록:
			  - 모임원 ID, 닉네임, 색상
			  - 선택한 책 정보 (ID, 제목, 표지 이미지)
			  - 책을 선택하지 않은 모임원: 책 정보는 null로 반환

			**사용 사례:**
			- 책 고르기 화면에서 현재 모임원들의 선택 상태 확인
			- 모임 대시보드에서 전체 현황 표시
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = GroupMembersBookResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임을 찾을 수 없음 (G001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/{groupId}/members/books")
	public ApiResponse<GroupMembersBookResponse> getGroupMembersWithBooks(
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		GroupMembersBookResponse response = groupService.getGroupMembersWithBooks(groupId);
		return ApiResponse.success(response);
	}

	/**
	 * 교환독서 시작 (E.2.4 교환독서 시작 버튼)
	 */
	@Operation(
		summary = "교환독서 시작",
		description = """
			교환독서를 시작하고 모임원들에게 책을 배정합니다.

			**프로세스:**
			1. 요청자가 모임장인지 확인합니다
			2. 모임 상태가 WAITING인지 확인합니다
			3. 책을 선택하지 않은 모임원에게 랜덤으로 책을 배정합니다
			4. 모임 상태를 IN_PROGRESS로 변경합니다
			5. 시작 시간을 기록하고 첫 회차 배정을 생성합니다

			**검증 규칙:**
			- 모임장만 시작할 수 있습니다 (M006)
			- 이미 시작된 모임은 다시 시작할 수 없습니다 (G007)

			**자동 책 배정:**
			- 책을 선택하지 않은 모임원에게는 랜덤으로 책이 배정됩니다

			**교환독서 시작:**
			- 모임 상태: WAITING → IN_PROGRESS
			- 시작 버튼 클릭한 날짜를 기준으로 교환일 카운트
			- 독서 기간에 따라 교환 일정이 계산됩니다
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "교환독서 시작 성공",
			content = @Content(schema = @Schema(implementation = GroupStartResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "이미 시작된 모임 (G007)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "403",
			description = "모임장만 실행 가능 (M006)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 모임원을 찾을 수 없음 (G001, M001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PatchMapping("/{groupId}/start")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<GroupStartResponse> startGroup(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		GroupStartResponse response = groupService.startGroup(groupId, userId);
		return ApiResponse.success(response, "교환독서를 시작했습니다.");
	}

	/**
	 * 모임 탈퇴 (A.3.1 모임 탈퇴하기 → A.3.2 모임 탈퇴 완료)
	 */
	@Operation(
		summary = "모임 탈퇴",
		description = """
			모임에서 탈퇴합니다.

			**프로세스:**
			1. 모임원 정보를 확인합니다
			2. 탈퇴 처리: 닉네임 → "탈퇴한 모임원", 색상 → "GRAY"로 변경
			3. 사용자의 남은 활성 모임 수를 반환합니다

			**UI 처리:**
			- 탈퇴 전 안내문구 표시
			- 사용자가 안내사항을 확인했다는 체크박스를 터치해야 탈퇴 가능
			- 탈퇴 완료 후 홈으로 이동

			**탈퇴 후 영향:**
			- 교환독서가 진행 중인 경우에도 탈퇴 가능
			- 탈퇴 후에도 다른 모임원들은 정상적으로 교환독서 진행
			- 탈퇴한 모임원의 닉네임은 "탈퇴한 모임원"으로 표시됨
			- 색상은 회색(GRAY)으로 변경되어 표시됨
			- 탈퇴한 사용자가 선택한 책을 읽고 있던 모임원이 있다면, 해당 책은 읽는 사람이 없는 상태가 될 수 있음
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "모임 탈퇴 성공",
			content = @Content(schema = @Schema(implementation = GroupWithdrawResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "이미 탈퇴한 모임원 (M007)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 모임원을 찾을 수 없음 (G001, M001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@DeleteMapping("/{groupId}/members")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<GroupWithdrawResponse> withdrawFromGroup(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		GroupWithdrawResponse response = memberService.withdrawFromGroup(userId, groupId);
		return ApiResponse.success(response);
	}

	/**
	 * G.2.0 재시작 가능 여부 확인
	 */
	@Operation(
		summary = "재시작 가능 여부 확인",
		description = """
			현재 모임의 재시작 가능 여부를 확인합니다.

			**프로세스:**
			1. 현재 최대 회차를 확인합니다
			2. 활성 모임원 수를 확인합니다
			3. 모든 회차를 완료했는지 계산합니다 (현재 회차 % 모임원 수 == 0)
			4. 재시작 가능 여부와 현재 라운드를 반환합니다

			**재시작 가능 조건:**
			- 모임원 수만큼의 회차를 모두 완료한 경우
			- 예: 4명의 모임원이면 4회차, 8회차, 12회차... 완료 시 재시작 가능

			**반환 정보:**
			- 재시작 가능 여부 (canRestart)
			- 현재 회차와 총 회차
			- 현재 라운드 정보
			- 안내 메시지
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "조회 성공",
			content = @Content(schema = @Schema(implementation = RestartStatusResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임을 찾을 수 없음 (G001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@GetMapping("/{groupId}/restart/status")
	public ApiResponse<RestartStatusResponse> getRestartStatus(
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		RestartStatusResponse response = groupService.getRestartStatus(groupId);
		return ApiResponse.success(response);
	}

	/**
	 * G.2.3 재시작 (모임장 전용)
	 */
	@Operation(
		summary = "모임 재시작",
		description = """
			모든 회차를 완료한 후 모임을 재시작합니다.

			**프로세스:**
			1. 요청자가 모임장인지 확인합니다
			2. 재시작 가능 여부를 확인합니다 (모든 회차 완료)
			3. 책을 선택하지 않은 모임원에게 랜덤으로 책을 배정합니다
			4. 새로운 회차(1회차)의 배정을 생성합니다
			5. 이전 라운드의 모든 데이터는 보존됩니다

			**검증 규칙:**
			- 모임장만 재시작할 수 있습니다 (M006)
			- 모든 회차를 완료해야 재시작 가능합니다

			**재시작 효과:**
			- 새로운 라운드 시작 (라운드 번호 증가)
			- 책을 선택하지 않은 모임원: 랜덤 책 자동 배정
			- 새로운 1회차 배정 생성
			- 이전 라운드의 ReadingAssignment 데이터는 모두 보존됨

			**데이터 보존:**
			- 이전 회차의 모든 배정 정보
			- 한줄평
			- 독서 기록
			→ 모두 삭제되지 않고 영구 보존
			"""
	)
	@ApiResponses(value = {
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "200",
			description = "재시작 성공",
			content = @Content(schema = @Schema(implementation = GroupStartResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "400",
			description = "재시작 불가 - 모든 회차 미완료 (G009)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "403",
			description = "모임장만 실행 가능 (M006)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		),
		@io.swagger.v3.oas.annotations.responses.ApiResponse(
			responseCode = "404",
			description = "모임 또는 모임원을 찾을 수 없음 (G001, M001)",
			content = @Content(schema = @Schema(implementation = ErrorResponse.class))
		)
	})
	@PostMapping("/{groupId}/restart")
	@ResponseStatus(HttpStatus.OK)
	public ApiResponse<GroupStartResponse> restartGroup(
		@Parameter(hidden = true) @AuthUser String userId,
		@Parameter(description = "모임 ID", example = "1")
		@PathVariable Long groupId
	) {
		GroupStartResponse response = groupService.restartGroup(groupId, userId);
		return ApiResponse.success(response, "모임을 재시작했습니다.");
	}
}
