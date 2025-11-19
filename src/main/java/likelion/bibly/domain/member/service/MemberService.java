package likelion.bibly.domain.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.dto.GroupWithdrawResponse;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

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
		// 모임 검증
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 모임원 검증
		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// 이미 탈퇴한 모임원인지 확인
		if (member.getStatus() == MemberStatus.WITHDRAWN) {
			throw new BusinessException(ErrorCode.MEMBER_ALREADY_WITHDRAWN);
		}

		// 탈퇴 처리 (닉네임 -> "탈퇴한 모임원", 색상 -> "GRAY")
		member.withdraw();

		// 남은 활성 모임 수 조회
		long remainingGroupCount = memberRepository.countByUserIdAndStatus(userId, MemberStatus.ACTIVE);

		return GroupWithdrawResponse.builder()
			.groupId(group.getGroupId())
			.groupName(group.getGroupName())
			.remainingGroupCount(remainingGroupCount)
			.message("모임에서 탈퇴했습니다.")
			.build();
	}
}
