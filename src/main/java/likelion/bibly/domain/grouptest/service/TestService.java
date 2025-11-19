package likelion.bibly.domain.grouptest.service;

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
	 * 현재 회차부터 모임원 수만큼의 회차를 모두 생성하고 COMPLETED 상태로 변경
	 *
	 * @param groupId 모임 ID
	 * @return 완료 메시지
	 */
	@Transactional
	public String completeAllCycles(Long groupId) {
		// 모임 조회
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		if (memberCount == 0) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}

		// 현재 최대 회차 확인
		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);
		Integer currentMaxCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		// 필요한 회차 계산 (다음 라운드 완료까지)
		int currentRound = (currentMaxCycle - 1) / memberCount + 1;
		int targetCycle = currentRound * memberCount; // 현재 라운드 완료 회차

		// 현재 회차부터 목표 회차까지 배정 생성
		for (int cycle = currentMaxCycle + 1; cycle <= targetCycle; cycle++) {
			assignmentService.rotateBooks(groupId);
		}

		// 모든 모임원의 책 선택 초기화 (재시작을 위해)
		for (Member member : activeMembers) {
			member.clearSelectedBook();
		}

		// 모임 상태를 COMPLETED로 변경
		group.complete();

		return String.format("모임 ID %d: %d회차부터 %d회차까지 생성 완료. 모든 모임원 책 선택 초기화. 모임 상태: COMPLETED",
			groupId, currentMaxCycle + 1, targetCycle);
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
