package likelion.bibly.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.dto.GroupWithdrawResponse;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 모임원 관리 서비스
 * 모임 탈퇴 등의 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;
	private final GroupRepository groupRepository;
	private final ReadingAssignmentRepository assignmentRepository;



	/**
	 * 모임 탈퇴
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @param groupId 탈퇴할 모임 ID
	 * @return 탈퇴 완료 정보 (남은 활성 모임 수 포함)
	 * @throws BusinessException G001, M001, M007
	 */
	@Transactional
	public GroupWithdrawResponse withdrawFromGroup(String userId, Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// 이미 탈퇴한 모임원인지 확인
		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
		}

		// 모임장이 탈퇴하는 경우, 다음 멤버에게 리더 역할 이전
		boolean wasLeader = member.getRole() == likelion.bibly.domain.member.enums.MemberRole.LEADER;

		// 탈퇴 처리 (닉네임 -> "탈퇴한 모임원", 색상 -> "GRAY")
		member.withdraw();

		long remainingGroupCount = memberRepository.countByUserIdAndStatus(userId, MemberStatus.ACTIVE);

		// 탈퇴 후 남은 활성 모임원 조회
		List<Member> remainingMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		if (wasLeader && !remainingMembers.isEmpty()) {
			// 리더가 탈퇴했고 아직 활성 멤버가 남아있는 경우
			// 가장 먼저 가입한 멤버에게 리더 역할 이전 (memberId가 작을수록 먼저 가입)
			Member newLeader = remainingMembers.stream()
				.min((m1, m2) -> Long.compare(m1.getMemberId(), m2.getMemberId()))
				.orElse(null);

			if (newLeader != null) {
				newLeader.promoteToLeader();
			}
		}

		Long groupIdForResponse = group.getGroupId();
		String groupNameForResponse = group.getGroupName();

		// 모든 멤버가 탈퇴한 경우 모임 삭제
		if (remainingMembers.isEmpty()) {
			assignmentRepository.deleteByGroup_GroupId(groupId);
			memberRepository.deleteAll(memberRepository.findByGroup_GroupId(groupId));
			groupRepository.deleteById(groupId);
		}

		return GroupWithdrawResponse.builder()
			.groupId(groupIdForResponse)
			.groupName(groupNameForResponse)
			.remainingGroupCount(remainingGroupCount)
			.message("모임에서 탈퇴했습니다.")
			.build();
	}

    public List<Long> getActiveMemberIdsByUserId(String userId) {
        return memberRepository.findActiveMemberIdsByUserId(userId, MemberStatus.ACTIVE);
    }
}