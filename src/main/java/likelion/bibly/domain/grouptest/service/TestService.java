package likelion.bibly.domain.grouptest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.assignment.service.AssignmentService;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

/**
 * 테스트용 서비스
 * 개발 및 테스트 환경에서만 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TestService {

	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final ReadingAssignmentRepository assignmentRepository;
	private final AssignmentService assignmentService;

	/**
	 * 모든 회차를 강제로 완료시키는 테스트용 메서드
	 * 현재 회차부터 모임원 수만큼의 회차를 모두 생성하고,
	 * 마지막 회차의 기간을 현재 시간 이전으로 설정하여 재시작 가능 상태로 만듭니다.
	 *
	 * @param groupId 모임 ID
	 * @return 완료 메시지
	 */
	@Transactional
	public String completeAllCycles(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		if (memberCount == 0) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}

		// 모임장의 userId 조회 (rotateBooks 호출용)
		Member leader = activeMembers.stream()
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
		String leaderUserId = leader.getUserId();

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);
		Integer currentMaxCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		int currentRound = (currentMaxCycle - 1) / memberCount + 1;
		int targetCycle = currentRound * memberCount;

		for (int cycle = currentMaxCycle + 1; cycle <= targetCycle; cycle++) {
			assignmentService.rotateBooks(groupId, leaderUserId);
		}

		List<ReadingAssignment> updatedAssignments = assignmentRepository.findByGroup_GroupId(groupId);
		LocalDateTime pastTime = LocalDateTime.now().minusHours(1);

		for (ReadingAssignment assignment : updatedAssignments) {
			if (assignment.getCycleNumber().equals(targetCycle)) {
				assignment.updateEndDate(pastTime);
			}
		}

		// 모든 회차 완료 후 선택된 책 초기화
		activeMembers.forEach(Member::clearSelectedBook);

		// 탈퇴한 모임원 삭제 (외래키 제약으로 인해 먼저 배정 삭제)
		List<Member> withdrawnMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.WITHDRAWN);
		for (Member withdrawn : withdrawnMembers) {
			// 탈퇴한 멤버의 모든 배정 삭제
			List<ReadingAssignment> assignmentsToDelete = assignmentRepository.findByMember_MemberId(withdrawn.getMemberId());
			assignmentRepository.deleteAll(assignmentsToDelete);
		}
		memberRepository.deleteAll(withdrawnMembers);

		return String.format("모임 ID %d: %d회차부터 %d회차까지 생성 완료. %d회차 기간 만료 처리. 재시작 가능 상태로 설정됨",
			groupId, currentMaxCycle + 1, targetCycle, targetCycle);
	}

	/**
	 * 모임 상태 초기화 (WAITING으로 되돌림)
	 *
	 * @param groupId 모임 ID
	 * @return 초기화 메시지
	 */
	@Transactional
	public String resetGroup(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 모든 배정 삭제
		List<ReadingAssignment> assignments = assignmentRepository.findByGroup_GroupId(groupId);
		assignmentRepository.deleteAll(assignments);

		return String.format("모임 ID %d: 모든 배정 삭제 완료", groupId);
	}
}