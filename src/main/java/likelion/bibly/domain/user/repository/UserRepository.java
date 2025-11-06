package likelion.bibly.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;

public interface UserRepository extends JpaRepository<User, String> {
	Optional<User> findByUserIdAndStatus(String userId, UserStatus status);
}
