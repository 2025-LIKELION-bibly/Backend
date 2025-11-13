package likelion.bibly.domain.user.service;

import likelion.bibly.domain.navigator.service.NavigatorService;
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
}
