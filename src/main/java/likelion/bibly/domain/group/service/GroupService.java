package likelion.bibly.domain.group.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.dto.GroupCreateRequest;
import likelion.bibly.domain.group.dto.GroupCreateResponse;
import likelion.bibly.domain.group.dto.InviteCodeValidateResponse;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.group.util.InviteCodeGenerator;
import likelion.bibly.domain.member.dto.GroupJoinRequest;
import likelion.bibly.domain.member.dto.GroupJoinResponse;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.member.util.MemberColorUtil;
import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

/**
 * 모임 관리 서비스
 * 모임 생성, 초대 코드 검증, 모임 참여 등의 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class GroupService {
	private static final int MAX_MEMBERS = 8;

	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final UserRepository userRepository;
	private final BookRepository bookRepository;

	/**
	 * 모임 생성
	 * 4자리 초대 코드를 자동 생성하고, 생성자를 모임장(LEADER)으로 등록합니다.
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @param request 모임 생성 요청 (groupName, readingPeriod, nickname, color)
	 * @return 생성된 모임 정보
	 * @throws BusinessException U001, M004, M005, C001, C002
	 */
	@Transactional
	public GroupCreateResponse createGroup(String userId, GroupCreateRequest request) {
		// 사용자 검증
		User user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 닉네임, 색상 검증
		validateNickname(request.nickname());

		if (!MemberColorUtil.isValidColor(request.color())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}

		// 초대 코드 생성 (중복되지 않을 때까지 생성)
		String inviteCode = generateUniqueInviteCode();

		// 그룹 생성
		Group group = Group.builder()
			.groupName(request.groupName())
			.readingPeriod(request.readingPeriod())
			.inviteCode(inviteCode)
			.build();

		Group savedGroup = groupRepository.save(group);

		// 모임장 멤버 생성
		Member leader = Member.builder()
			.group(savedGroup)
			.userId(userId)
			.nickname(request.nickname())
			.color(request.color())
			.role(MemberRole.LEADER)
			.build();

		Member savedLeader = memberRepository.save(leader);

		return GroupCreateResponse.builder()
			.groupId(savedGroup.getGroupId())
			.groupName(savedGroup.getGroupName())
			.inviteCode(savedGroup.getInviteCode())
			.readingPeriod(savedGroup.getReadingPeriod())
			.memberId(savedLeader.getMemberId())
			.build();
	}

	/**
	 * 초대 코드 검증
	 * 초대 코드로 모임 정보를 조회하고, 현재 모임원 목록과 사용 가능한 색상을 반환합니다.
	 *
	 * @param inviteCode 4자리 숫자 초대 코드
	 * @return 모임 정보, 모임원 목록, 사용 가능한 색상
	 * @throws BusinessException G002, G004
	 */
	public InviteCodeValidateResponse validateInviteCode(String inviteCode) {
		// 초대 코드로 그룹 조회
		Group group = groupRepository.findByInviteCode(inviteCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_INVITE_CODE));

		// 현재 모임원 수 확인
		long memberCount = memberRepository.countByGroup_GroupIdAndStatus(group.getGroupId(), MemberStatus.ACTIVE);

		// 모임원이 가득 찼는지 확인
		if (memberCount >= MAX_MEMBERS) {
			throw new BusinessException(ErrorCode.GROUP_FULL);
		}

		// 모임원 정보 조회
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(group.getGroupId(), MemberStatus.ACTIVE);

		List<InviteCodeValidateResponse.MemberSummary> memberSummaries = members.stream()
			.map(member -> InviteCodeValidateResponse.MemberSummary.builder()
				.nickname(member.getNickname())
				.color(member.getColor())
				.hasSelectedBook(member.getSelectedBookId() != null)
				.build())
			.collect(Collectors.toList());

		// 사용 가능한 색상 목록
		List<String> usedColors = members.stream()
			.map(Member::getColor)
			.collect(Collectors.toList());
		List<String> availableColors = MemberColorUtil.getAvailableColors(usedColors);

		return InviteCodeValidateResponse.builder()
			.groupId(group.getGroupId())
			.groupName(group.getGroupName())
			.readingPeriod(group.getReadingPeriod())
			.memberCount(memberCount)
			.members(memberSummaries)
			.availableColors(availableColors)
			.build();
	}

	/**
	 * 모임 참여
	 * 닉네임과 색상 중복을 검증하고, 사용자를 모임원(MEMBER)으로 등록합니다.
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @param groupId 참여할 모임 ID
	 * @param request 참여 요청 (nickname, color)
	 * @return 참여 완료 정보 및 모임원 목록
	 * @throws BusinessException U001, U003, G001, G004, M002, M003, M004, M005, C001
	 */
	@Transactional
	public GroupJoinResponse joinGroup(String userId, Long groupId, GroupJoinRequest request) {
		// 사용자 검증
		User user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 그룹 검증
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 이미 가입한 사용자인지 확인
		memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.ifPresent(member -> {
				throw new BusinessException(ErrorCode.DUPLICATE_USER);
			});

		// 모임원 수 확인
		long memberCount = memberRepository.countByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		if (memberCount >= MAX_MEMBERS) {
			throw new BusinessException(ErrorCode.GROUP_FULL);
		}

		// 닉네임, 색상 검증
		validateNickname(request.nickname());
		if (memberRepository.existsByGroup_GroupIdAndNicknameAndStatus(groupId, request.nickname(),
			MemberStatus.ACTIVE)) {
			throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
		}

		if (!MemberColorUtil.isValidColor(request.color())) {
			throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
		}
		if (memberRepository.existsByGroup_GroupIdAndColorAndStatus(groupId, request.color(), MemberStatus.ACTIVE)) {
			throw new BusinessException(ErrorCode.DUPLICATE_COLOR);
		}

		// 멤버 생성
		Member member = Member.builder()
			.group(group)
			.userId(userId)
			.nickname(request.nickname())
			.color(request.color())
			.role(MemberRole.MEMBER)
			.build();

		Member savedMember = memberRepository.save(member);

		// 모임원 정보 조회
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		List<GroupJoinResponse.MemberInfo> memberInfos = members.stream()
			.map(m -> {
				String bookTitle = null;
				if (m.getSelectedBookId() != null) {
					bookTitle = bookRepository.findById(m.getSelectedBookId())
						.map(book -> book.getTitle())
						.orElse(null);
				}

				return GroupJoinResponse.MemberInfo.builder()
					.nickname(m.getNickname())
					.color(m.getColor())
					.selectedBookId(m.getSelectedBookId())
					.selectedBookTitle(bookTitle)
					.hasSelectedBook(m.getSelectedBookId() != null)
					.build();
			})
			.collect(Collectors.toList());

		return GroupJoinResponse.builder()
			.memberId(savedMember.getMemberId())
			.groupId(group.getGroupId())
			.groupName(group.getGroupName())
			.nickname(savedMember.getNickname())
			.color(savedMember.getColor())
			.members(memberInfos)
			.build();
	}

	/**
	 * 중복되지 않는 4자리 초대 코드 생성
	 */
	private String generateUniqueInviteCode() {
		String inviteCode;
		int attempts = 0;
		do {
			inviteCode = InviteCodeGenerator.generate();
			attempts++;
			if (attempts > 100) {
				throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
			}
		} while (groupRepository.existsByInviteCode(inviteCode));
		return inviteCode;
	}

	/**
	 * 닉네임 형식 검증 (1~8자, 영문/한글/숫자만 허용)
	 */
	private void validateNickname(String nickname) {
		if (nickname.length() > 8) {
			throw new BusinessException(ErrorCode.INVALID_NICKNAME_LENGTH);
		}
		// 영문, 한글, 숫자만 허용
		if (!nickname.matches("^[a-zA-Z0-9가-힣]+$")) {
			throw new BusinessException(ErrorCode.INVALID_NICKNAME_FORMAT);
		}
	}
}
