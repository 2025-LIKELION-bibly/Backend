package likelion.bibly.domain.user.service;

import likelion.bibly.domain.group.dto.response.UserGroupsInfoResponse;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.enums.GroupStatus;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.navigator.service.NavigatorService;
import likelion.bibly.domain.user.dto.response.ServiceWithdrawResponse;
import likelion.bibly.domain.user.dto.response.UserCreateResponse;
import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import likelion.bibly.global.util.UuidGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * UserService 단위 테스트
 * Service 계층의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UuidGenerator uuidGenerator;

    @Mock
    private NavigatorService navigatorService;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("사용자 생성 성공 테스트")
    void createUserSuccessTest() {
        // Given
        String generatedUserId = "test-uuid-1234";
        User user = User.builder().userId(generatedUserId).build();

        given(uuidGenerator.generate()).willReturn(generatedUserId);
        given(userRepository.save(any(User.class))).willReturn(user);

        // When
        UserCreateResponse response = userService.createUser();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(generatedUserId);
        verify(uuidGenerator).generate();
        verify(userRepository).save(any(User.class));
        verify(navigatorService).createDefaultNavigator(any(User.class));
    }

    @Test
    @DisplayName("사용자 검증 성공 테스트")
    void validateUserSuccessTest() {
        // Given
        String userId = "valid-user-id";
        User user = User.builder().userId(userId).build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));

        // When & Then
        assertThatCode(() -> userService.validateUser(userId))
                .doesNotThrowAnyException();

        verify(userRepository).findByUserIdAndStatus(userId, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("사용자 검증 실패 - 존재하지 않는 사용자")
    void validateUserFailNotFoundTest() {
        // Given
        String invalidUserId = "invalid-user-id";

        given(userRepository.findByUserIdAndStatus(invalidUserId, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.validateUser(invalidUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("사용자 모임 정보 조회 성공 테스트")
    void getUserGroupsInfoSuccessTest() {
        // Given
        String userId = "test-user-id";
        User user = User.builder().userId(userId).build();

        Group group1 = Group.builder()
                .groupName("모임1")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Group group2 = Group.builder()
                .groupName("모임2")
                .readingPeriod(10)
                .inviteCode("5678")
                .build();

        Member member1 = Member.builder()
                .group(group1)
                .userId(userId)
                .nickname("닉네임1")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();

        Member member2 = Member.builder()
                .group(group2)
                .userId(userId)
                .nickname("닉네임2")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE))
                .willReturn(List.of(member1, member2));

        // When
        UserGroupsInfoResponse response = userService.getUserGroupsInfo(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getGroups()).hasSize(2);
        assertThat(response.getGroups().get(0).getGroupName()).isEqualTo("모임1");
        assertThat(response.getGroups().get(1).getGroupName()).isEqualTo("모임2");
        verify(userRepository).findByUserIdAndStatus(userId, UserStatus.ACTIVE);
        verify(memberRepository).findByUserIdAndStatus(userId, MemberStatus.ACTIVE);
    }

    @Test
    @DisplayName("사용자 모임 정보 조회 - 가입한 모임 없음")
    void getUserGroupsInfoEmptyGroupsTest() {
        // Given
        String userId = "lonely-user-id";
        User user = User.builder().userId(userId).build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE))
                .willReturn(List.of());

        // When
        UserGroupsInfoResponse response = userService.getUserGroupsInfo(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getGroups()).isEmpty();
    }

    @Test
    @DisplayName("서비스 탈퇴 성공 테스트")
    void withdrawFromServiceSuccessTest() {
        // Given
        String userId = "withdraw-user-id";
        User user = User.builder().userId(userId).build();

        Group group = Group.builder()
                .groupName("탈퇴 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("탈퇴자")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE))
                .willReturn(List.of(member));

        // When
        ServiceWithdrawResponse response = userService.withdrawFromService(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getMessage()).contains("탈퇴했습니다");
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(member.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        verify(userRepository).findByUserIdAndStatus(userId, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("서비스 탈퇴 실패 - 이미 탈퇴한 사용자")
    void withdrawFromServiceFailAlreadyWithdrawnTest() {
        // Given
        String userId = "already-withdrawn-user-id";
        User user = User.builder().userId(userId).build();
        user.withdraw(); // 이미 탈퇴됨

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));

        // When & Then
        assertThatThrownBy(() -> userService.withdrawFromService(userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_ALREADY_WITHDRAWN);
    }

    @Test
    @DisplayName("서비스 탈퇴 실패 - 사용자 없음")
    void withdrawFromServiceFailUserNotFoundTest() {
        // Given
        String invalidUserId = "invalid-user-id";

        given(userRepository.findByUserIdAndStatus(invalidUserId, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.withdrawFromService(invalidUserId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("서비스 탈퇴 - 여러 모임에 가입된 경우")
    void withdrawFromServiceMultipleGroupsTest() {
        // Given
        String userId = "multi-group-user-id";
        User user = User.builder().userId(userId).build();

        Group group1 = Group.builder()
                .groupName("모임1")
                .readingPeriod(14)
                .inviteCode("1111")
                .build();

        Group group2 = Group.builder()
                .groupName("모임2")
                .readingPeriod(10)
                .inviteCode("2222")
                .build();

        Group group3 = Group.builder()
                .groupName("모임3")
                .readingPeriod(7)
                .inviteCode("3333")
                .build();

        Member member1 = Member.builder()
                .group(group1)
                .userId(userId)
                .nickname("닉1")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();

        Member member2 = Member.builder()
                .group(group2)
                .userId(userId)
                .nickname("닉2")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        Member member3 = Member.builder()
                .group(group3)
                .userId(userId)
                .nickname("닉3")
                .color("GREEN")
                .role(MemberRole.MEMBER)
                .build();

        List<Member> members = List.of(member1, member2, member3);

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(memberRepository.findByUserIdAndStatus(userId, MemberStatus.ACTIVE))
                .willReturn(members);

        // When
        ServiceWithdrawResponse response = userService.withdrawFromService(userId);

        // Then
        assertThat(response).isNotNull();
        assertThat(user.getStatus()).isEqualTo(UserStatus.WITHDRAWN);
        assertThat(member1.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(member2.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
        assertThat(member3.getStatus()).isEqualTo(MemberStatus.WITHDRAWN);
    }
}
