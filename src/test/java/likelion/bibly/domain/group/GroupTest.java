package likelion.bibly.domain.group;

import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.enums.GroupStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Group 엔티티 단위 테스트
 * 엔티티의 핵심 비즈니스 로직을 검증합니다.
 */
class GroupTest {

    @Test
    @DisplayName("Group 엔티티 빌더 생성 테스트")
    void createGroupWithBuilderTest() {
        // Given
        String groupName = "Test Group";
        Integer readingPeriod = 14;
        String inviteCode = "1234";

        // When
        Group group = Group.builder()
                .groupName(groupName)
                .readingPeriod(readingPeriod)
                .inviteCode(inviteCode)
                .build();

        // Then
        assertThat(group).isNotNull();
        assertThat(group.getGroupName()).isEqualTo(groupName);
        assertThat(group.getReadingPeriod()).isEqualTo(readingPeriod);
        assertThat(group.getInviteCode()).isEqualTo(inviteCode);
        assertThat(group.getStatus()).isEqualTo(GroupStatus.WAITING);
        assertThat(group.getCreatedAt()).isNotNull();
        assertThat(group.getInviteCodeExpiredAt()).isNotNull();
    }

    @Test
    @DisplayName("Group 시작 기능 테스트")
    void startGroupTest() {
        // Given
        Group group = Group.builder()
                .groupName("Start Test Group")
                .readingPeriod(10)
                .inviteCode("5678")
                .build();

        // When
        group.start();

        // Then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.IN_PROGRESS);
        assertThat(group.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("Group 완료 기능 테스트")
    void completeGroupTest() {
        // Given
        Group group = Group.builder()
                .groupName("Complete Test Group")
                .readingPeriod(7)
                .inviteCode("9012")
                .build();
        group.start();

        // When
        group.complete();

        // Then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.COMPLETED);
    }

    @Test
    @DisplayName("Group 리셋 기능 테스트")
    void resetGroupTest() {
        // Given
        Group group = Group.builder()
                .groupName("Reset Test Group")
                .readingPeriod(14)
                .inviteCode("3456")
                .build();
        group.start();

        // When
        group.reset();

        // Then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.WAITING);
        assertThat(group.getStartedAt()).isNull();
    }

    @Test
    @DisplayName("초대 코드 유효성 검증 테스트")
    void isInviteCodeValidTest() {
        // Given
        Group group = Group.builder()
                .groupName("Invite Code Test Group")
                .readingPeriod(10)
                .inviteCode("7890")
                .build();

        // When
        boolean isValid = group.isInviteCodeValid();

        // Then - 방금 생성된 그룹은 초대 코드가 유효함
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Group 상태 변경 흐름 테스트")
    void groupStatusFlowTest() {
        // Given
        Group group = Group.builder()
                .groupName("Status Flow Test Group")
                .readingPeriod(14)
                .inviteCode("2580")
                .build();

        // Then - WAITING 상태
        assertThat(group.getStatus()).isEqualTo(GroupStatus.WAITING);

        // When & Then - IN_PROGRESS 상태
        group.start();
        assertThat(group.getStatus()).isEqualTo(GroupStatus.IN_PROGRESS);
        assertThat(group.getStartedAt()).isNotNull();

        // When & Then - COMPLETED 상태
        group.complete();
        assertThat(group.getStatus()).isEqualTo(GroupStatus.COMPLETED);

        // When & Then - RESET to WAITING
        group.reset();
        assertThat(group.getStatus()).isEqualTo(GroupStatus.WAITING);
        assertThat(group.getStartedAt()).isNull();
    }

    @Test
    @DisplayName("Group 생성 시 기본값 검증")
    void groupDefaultValuesTest() {
        // Given & When
        Group group = Group.builder()
                .groupName("Default Test Group")
                .readingPeriod(10)
                .inviteCode("1111")
                .build();

        // Then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.WAITING);
        assertThat(group.getCreatedAt()).isNotNull();
        assertThat(group.getStartedAt()).isNull();
        assertThat(group.getInviteCodeExpiredAt()).isNotNull();
    }

    @Test
    @DisplayName("여러 상태의 Group 테스트")
    void multipleGroupStatusTest() {
        // Given
        Group waitingGroup = Group.builder()
                .groupName("Waiting Group")
                .readingPeriod(10)
                .inviteCode("1111")
                .build();

        Group inProgressGroup = Group.builder()
                .groupName("In Progress Group")
                .readingPeriod(14)
                .inviteCode("2222")
                .build();
        inProgressGroup.start();

        Group completedGroup = Group.builder()
                .groupName("Completed Group")
                .readingPeriod(7)
                .inviteCode("3333")
                .build();
        completedGroup.start();
        completedGroup.complete();

        // Then
        assertThat(waitingGroup.getStatus()).isEqualTo(GroupStatus.WAITING);
        assertThat(inProgressGroup.getStatus()).isEqualTo(GroupStatus.IN_PROGRESS);
        assertThat(completedGroup.getStatus()).isEqualTo(GroupStatus.COMPLETED);
    }
}
