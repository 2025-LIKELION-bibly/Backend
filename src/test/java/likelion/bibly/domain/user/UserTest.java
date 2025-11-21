package likelion.bibly.domain.user;

import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * User 엔티티 단위 테스트
 * 엔티티의 핵심 비즈니스 로직을 검증합니다.
 */
class UserTest {

    @Test
    @DisplayName("User 엔티티 빌더 테스트")
    void createUserWithBuilderTest() {
        // Given
        String userId = "test-user-123";

        // When
        User user = User.builder()
                .userId(userId)
                .build();

        // Then
        assertThat(user).isNotNull();
        assertThat(user.getUserId()).isEqualTo(userId);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getWithdrawnAt()).isNull();
    }

    @Test
    @DisplayName("User 탈퇴 기능 테스트")
    void withdrawUserTest() {
        // Given
        User user = User.builder()
                .userId("withdraw-test-user")
                .build();

        // When
        user.withdraw();

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getWithdrawnAt()).isNotNull();
        assertThat(user.getWithdrawnAt()).isBefore(java.time.LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("User 생성 시 기본값 검증")
    void userDefaultValuesTest() {
        // Given & When
        User user = User.builder()
                .userId("default-test-user")
                .build();

        // Then - 기본값 확인
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getWithdrawnAt()).isNull();
    }

    @Test
    @DisplayName("탈퇴한 User의 상태 확인")
    void withdrawnUserStatusTest() {
        // Given
        User user = User.builder()
                .userId("status-test-user")
                .build();

        // When
        user.withdraw();

        // Then
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(user.getWithdrawnAt()).isNotNull();

        // 생성 시간은 유지됨
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("여러 User 엔티티 생성 테스트")
    void multipleUsersTest() {
        // Given & When
        User user1 = User.builder().userId("user-1").build();
        User user2 = User.builder().userId("user-2").build();
        User user3 = User.builder().userId("user-3").build();

        // Then
        assertThat(user1.getUserId()).isEqualTo("user-1");
        assertThat(user2.getUserId()).isEqualTo("user-2");
        assertThat(user3.getUserId()).isEqualTo("user-3");

        assertThat(user1.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user2.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user3.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }
}
