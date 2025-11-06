package likelion.bibly.domain.user.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.bibly.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserCreateResponse {
	@Schema(description = "사용자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
	private String userId;

	public static UserCreateResponse from(User user) {
		return UserCreateResponse.builder()
			.userId(user.getUserId())
			.build();
	}
}
