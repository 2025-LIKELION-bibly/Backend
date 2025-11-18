package likelion.bibly.domain.user.service;

import java.util.List;

import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.navigator.service.NavigatorService;
import likelion.bibly.domain.user.dto.ServiceWithdrawResponse;
import likelion.bibly.domain.user.dto.response.UserCreateResponse;
import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import likelion.bibly.global.util.UuidGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final UuidGenerator uuidGenerator;
    private final NavigatorService navigatorService;
	private final MemberRepository memberRepository;

	@Override
	@Transactional
	public UserCreateResponse createUser() {
		String userId = uuidGenerator.generate();
		User user = User.builder()
			.userId(userId)
			.build();

		User savedUser = userRepository.save(user);

        // User 생성 직후 해당 User와 연결된 Navigator 초기 데이터 생성
        navigatorService.createDefaultNavigator(savedUser);
		return UserCreateResponse.from(user);
	}

	@Override
	public void validateUser(String userId) {
		userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
	}

	/**
	 * 서비스 탈퇴
	 * 사용자를 탈퇴 처리하고, 가입한 모든 모임에서도 탈퇴 처리합니다.
	 *
	 * @param userId 사용자 ID (헤더에서 추출)
	 * @return 탈퇴 완료 정보
	 * @throws BusinessException U001, U002
	 */
	@Override
	@Transactional
	public ServiceWithdrawResponse withdrawFromService(String userId) {
		// 사용자 검증
		User user = userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE)
			.orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

		// 이미 탈퇴한 사용자인지 확인
		if (user.getStatus() == UserStatus.WITHDRAWN) {
			throw new BusinessException(ErrorCode.USER_ALREADY_WITHDRAWN);
		}

		// 사용자의 모든 활성 모임원 정보 조회
		List<Member> activeMembers = memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE);

		// 모든 모임에서 탈퇴 처리
		for (Member member : activeMembers) {
			member.withdraw();
		}

		// 사용자 탈퇴 처리
		user.withdraw();

		return ServiceWithdrawResponse.builder()
			.userId(user.getUserId())
			.message("서비스에서 탈퇴했습니다. 그동안 이용해주셔서 감사합니다.")
			.build();
	}
}
