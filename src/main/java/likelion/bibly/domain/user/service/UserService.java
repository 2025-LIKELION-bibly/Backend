package likelion.bibly.domain.user.service;

import likelion.bibly.domain.user.dto.response.UserCreateResponse;

public interface UserService {
	UserCreateResponse createUser();

	void validateUser(String userId);
}
