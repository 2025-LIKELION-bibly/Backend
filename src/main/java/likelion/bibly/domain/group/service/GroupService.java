package likelion.bibly.domain.group.service;

import java.util.List;

import likelion.bibly.domain.group.dto.request.GroupCreateRequest;
import likelion.bibly.domain.group.dto.response.CurrentReadingAssignmentResponse;
import likelion.bibly.domain.group.dto.response.GroupCreateResponse;
import likelion.bibly.domain.group.dto.response.GroupMembersBookResponse;
import likelion.bibly.domain.group.dto.response.GroupStartResponse;
import likelion.bibly.domain.group.dto.response.InviteCodeValidateResponse;
import likelion.bibly.domain.group.dto.response.RestartStatusResponse;
import likelion.bibly.domain.member.dto.GroupJoinRequest;
import likelion.bibly.domain.member.dto.GroupJoinResponse;

/**
 * 모임 관리 서비스 인터페이스
 * 모임 생성, 초대 코드 검증, 모임 참여 등의 비즈니스 로직을 정의합니다.
 */
public interface GroupService {

	/**
	 * 모임 생성
	 * 4자리 초대 코드를 자동 생성하고, 생성자를 모임장(LEADER)으로 등록합니다.
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @param request 모임 생성 요청 (groupName, readingPeriod, nickname, color)
	 * @return 생성된 모임 정보
	 * @throws likelion.bibly.global.exception.BusinessException U001, M004, M005, C001, C002
	 */
	GroupCreateResponse createGroup(String userId, GroupCreateRequest request);

	/**
	 * 초대 코드 검증
	 * 초대 코드로 모임 정보를 조회하고, 현재 모임원 목록과 사용 가능한 색상을 반환합니다.
	 *
	 * @param inviteCode 4자리 숫자 초대 코드
	 * @return 모임 정보, 모임원 목록, 사용 가능한 색상
	 * @throws likelion.bibly.global.exception.BusinessException G002, G004
	 */
	InviteCodeValidateResponse validateInviteCode(String inviteCode);

	/**
	 * 모임 참여
	 * 닉네임과 색상 중복을 검증하고, 사용자를 모임원(MEMBER)으로 등록합니다.
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @param groupId 참여할 모임 ID
	 * @param request 참여 요청 (nickname, color)
	 * @return 참여 완료 정보 및 모임원 목록
	 * @throws likelion.bibly.global.exception.BusinessException U001, U003, G001, G004, M002, M003, M004, M005, C001
	 */
	GroupJoinResponse joinGroup(String userId, Long groupId, GroupJoinRequest request);

	/**
	 * 현재 배정받은 책 상태 조회
	 * 해당 모임의 모임원들이 현재 읽고 있는 책의 정보를 조회합니다.
	 * 시간 기준으로 현재 진행 중인 회차를 계산하여 반환합니다.
	 *
	 * @param groupId 모임 ID
	 * @return 모임 정보 + 모임원별 현재 배정 정보
	 * @throws likelion.bibly.global.exception.BusinessException G001
	 */
	CurrentReadingAssignmentResponse getCurrentReadingAssignments(Long groupId);

	/**
	 * 로그인한 사용자가 속한 모든 모임 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @return 사용자가 속한 모든 모임 정보 + 각 모임의 모임원 + 각자 선택한 책
	 */
	List<GroupMembersBookResponse> getMyGroups(String userId);

	/**
	 * 교환독서 시작
	 * 모든 모임원이 책을 선택했는지 확인하고, 모임 상태를 IN_PROGRESS로 변경합니다.
	 * 선택하지 않은 모임원에게는 랜덤으로 책을 배정합니다.
	 *
	 * @param groupId 모임 ID
	 * @param userId 요청자 사용자 ID (모임장 권한 확인)
	 * @return 교환독서 시작 정보 (모임원별 선택 책 정보 포함)
	 * @throws likelion.bibly.global.exception.BusinessException G001, G007, M006
	 */
	GroupStartResponse startGroup(Long groupId, String userId);

	/**
	 * 재시작 가능 여부 확인
	 * 현재 라운드의 마지막 회차 기간이 모두 지났는지 확인합니다.
	 *
	 * @param groupId 모임 ID
	 * @return 재시작 가능 여부 정보
	 * @throws likelion.bibly.global.exception.BusinessException G001, M001
	 */
	RestartStatusResponse getRestartStatus(Long groupId);

	/**
	 * 재시작
	 * 기존 배정과 한줄평, 독서 기록을 보존하고 새로운 회차를 추가로 생성합니다.
	 * 탈퇴한 모임원은 WITHDRAWN 상태로 유지됩니다 (데이터 무결성 보장).
	 *
	 * @param groupId 모임 ID
	 * @param userId 요청자 사용자 ID (모임장 권한 확인)
	 * @return 재시작 정보
	 * @throws likelion.bibly.global.exception.BusinessException G001, M006, G008
	 */
	GroupStartResponse restartGroup(Long groupId, String userId);
}
