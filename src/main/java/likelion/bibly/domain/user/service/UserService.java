package likelion.bibly.domain.user.service;

import likelion.bibly.domain.group.dto.response.UserGroupsInfoResponse;
import likelion.bibly.domain.user.dto.response.ServiceWithdrawResponse;
import likelion.bibly.domain.user.dto.response.UserCreateResponse;

public interface UserService {
	UserCreateResponse createUser();

	void validateUser(String userId);

	ServiceWithdrawResponse withdrawFromService(String userId);

	UserGroupsInfoResponse getUserGroupsInfo(String userId);
}
