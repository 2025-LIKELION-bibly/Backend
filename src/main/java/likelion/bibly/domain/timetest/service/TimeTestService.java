package likelion.bibly.domain.timetest.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimeTestService {

	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final ReadingAssignmentRepository assignmentRepository;

	/**
	 * 시간을 X일 앞으로 점프 (모든 배정을 X일 과거로 이동)
	 *
	 * @param groupId 모임 ID
	 * @param days 점프할 일수
	 * @return 결과 메시지
	 */
	@Transactional
	public String jumpForward(Long groupId, int days) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<ReadingAssignment> assignments = assignmentRepository.findByGroup_GroupId(groupId);

		if (assignments.isEmpty()) {
			throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
		}

		// 모든 배정의 시간을 days일 과거로 이동
		for (ReadingAssignment assignment : assignments) {
			LocalDateTime newStartDate = assignment.getStartDate().minusDays(days);
			LocalDateTime newEndDate = assignment.getEndDate().minusDays(days);

			assignment.updateDates(newStartDate, newEndDate);
		}

		// 현재 회차 계산
		LocalDateTime now = LocalDateTime.now();
		Integer currentCycle = assignments.stream()
			.filter(a -> !now.isBefore(a.getStartDate()) && !now.isAfter(a.getEndDate()))
			.map(ReadingAssignment::getCycleNumber)
			.findFirst()
			.orElse(null);

		if (currentCycle == null) {
			return String.format("모임 ID %d: %d일 점프 완료 (모든 회차 종료됨)", groupId, days);
		}

		return String.format("모임 ID %d: %d일 점프 완료 (현재 회차: %d)", groupId, days, currentCycle);
	}

	/**
	 * 다음 회차로 점프 (readingPeriod일 점프)
	 *
	 * @param groupId 모임 ID
	 * @return 결과 메시지
	 */
	@Transactional
	public String jumpToNextCycle(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		return jumpForward(groupId, group.getReadingPeriod());
	}

	/**
	 * 재시작 가능 상태로 점프 (모든 회차 완료)
	 *
	 * @param groupId 모임 ID
	 * @return 결과 메시지
	 */
	@Transactional
	public String jumpToRestart(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		if (memberCount == 0) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}

		// 전체 회차 완료를 위해 필요한 일수 = memberCount * readingPeriod + 1일 (여유)
		int daysToJump = memberCount * group.getReadingPeriod() + 1;

		String jumpResult = jumpForward(groupId, daysToJump);

		activeMembers.forEach(Member::clearSelectedBook);

		return String.format("%s. 재시작 가능 상태입니다. 모든 모임원의 책 선택이 초기화되었습니다.", jumpResult);
	}

}
