// Entity
package likelion.bibly.domain.user.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import likelion.bibly.domain.user.enums.UserStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@Column(name = "user_id", length = 40)
	private String userId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private UserStatus status;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "withdrawn_at")
	private LocalDateTime withdrawnAt;

	@Builder
	public User(String userId) {
		this.userId = userId;
		this.status = UserStatus.ACTIVE;
		this.createdAt = LocalDateTime.now();
	}

	public void withdraw() {
		this.status = UserStatus.WITHDRAWN;
		this.withdrawnAt = LocalDateTime.now();
	}
}
