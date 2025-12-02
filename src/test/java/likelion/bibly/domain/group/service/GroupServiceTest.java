package likelion.bibly.domain.group.service;

import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.assignment.service.AssignmentService;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.dto.request.GroupCreateRequest;
import likelion.bibly.domain.group.dto.response.GroupCreateResponse;
import likelion.bibly.domain.group.dto.response.InviteCodeValidateResponse;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.enums.GroupStatus;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.dto.GroupJoinRequest;
import likelion.bibly.domain.member.dto.GroupJoinResponse;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.user.entity.User;
import likelion.bibly.domain.user.enums.UserStatus;
import likelion.bibly.domain.user.repository.UserRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
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
 * GroupService 단위 테스트
 * Service 계층의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @InjectMocks
    private GroupServiceImpl groupService;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AssignmentService assignmentService;

    @Mock
    private ReadingAssignmentRepository assignmentRepository;

    @Test
    @DisplayName("모임 생성 성공 테스트")
    void createGroupSuccessTest() {
        // Given
        String userId = "test-user-id";
        GroupCreateRequest request = new GroupCreateRequest("테스트 모임", 14, "테스터", "RED");

        User user = User.builder().userId(userId).build();
        Group savedGroup = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member savedLeader = Member.builder()
                .group(savedGroup)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(groupRepository.existsByInviteCode(anyString())).willReturn(false);
        given(groupRepository.save(any(Group.class))).willReturn(savedGroup);
        given(memberRepository.save(any(Member.class))).willReturn(savedLeader);

        // When
        GroupCreateResponse response = groupService.createGroup(userId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getGroupName()).isEqualTo("테스트 모임");
        assertThat(response.getReadingPeriod()).isEqualTo(14);
        verify(userRepository).findByUserIdAndStatus(userId, UserStatus.ACTIVE);
        verify(groupRepository).save(any(Group.class));
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("모임 생성 실패 - 사용자 없음")
    void createGroupFailUserNotFoundTest() {
        // Given
        String userId = "invalid-user";
        GroupCreateRequest request = new GroupCreateRequest("테스트 모임", 14, "테스터", "RED");

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> groupService.createGroup(userId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("초대 코드 검증 성공 테스트")
    void validateInviteCodeSuccessTest() {
        // Given
        String inviteCode = "1234";
        Group group = Group.builder()
                .groupName("검증 테스트 모임")
                .readingPeriod(14)
                .inviteCode(inviteCode)
                .build();

        List<Member> members = List.of(
                Member.builder()
                        .group(group)
                        .userId("user1")
                        .nickname("멤버1")
                        .color("RED")
                        .role(MemberRole.LEADER)
                        .build()
        );

        given(groupRepository.findByInviteCode(inviteCode)).willReturn(Optional.of(group));
        given(memberRepository.countByGroup_GroupIdAndStatus(anyLong(), eq(MemberStatus.ACTIVE)))
                .willReturn(1L);
        given(memberRepository.findByGroup_GroupIdAndStatus(anyLong(), eq(MemberStatus.ACTIVE)))
                .willReturn(members);

        // When
        InviteCodeValidateResponse response = groupService.validateInviteCode(inviteCode);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getGroupName()).isEqualTo("검증 테스트 모임");
        assertThat(response.getMemberCount()).isEqualTo(1);
        verify(groupRepository).findByInviteCode(inviteCode);
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 잘못된 코드")
    void validateInviteCodeFailInvalidCodeTest() {
        // Given
        String invalidCode = "9999";
        given(groupRepository.findByInviteCode(invalidCode)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> groupService.validateInviteCode(invalidCode))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_INVITE_CODE);
    }

    @Test
    @DisplayName("모임 참여 성공 테스트")
    void joinGroupSuccessTest() {
        // Given
        String userId = "new-user";
        Long groupId = 1L;
        GroupJoinRequest request = new GroupJoinRequest("신규멤버", "BLUE");

        User user = User.builder().userId(userId).build();
        Group group = Group.builder()
                .groupName("참여 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member savedMember = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("신규멤버")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.empty());
        given(memberRepository.countByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(2L);
        given(memberRepository.existsByGroup_GroupIdAndNicknameAndStatus(groupId, "신규멤버", MemberStatus.ACTIVE))
                .willReturn(false);
        given(memberRepository.existsByGroup_GroupIdAndColorAndStatus(groupId, "BLUE", MemberStatus.ACTIVE))
                .willReturn(false);
        given(memberRepository.save(any(Member.class))).willReturn(savedMember);
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(savedMember));

        // When
        GroupJoinResponse response = groupService.joinGroup(userId, groupId, request);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getNickname()).isEqualTo("신규멤버");
        assertThat(response.getColor()).isEqualTo("BLUE");
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    @DisplayName("모임 참여 실패 - 중복 가입")
    void joinGroupFailDuplicateUserTest() {
        // Given
        String userId = "duplicate-user";
        Long groupId = 1L;
        GroupJoinRequest request = new GroupJoinRequest("중복", "RED");

        User user = User.builder().userId(userId).build();
        Group group = Group.builder()
                .groupName("중복 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member existingMember = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("기존멤버")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(existingMember));

        // When & Then
        assertThatThrownBy(() -> groupService.joinGroup(userId, groupId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_USER);
    }

    @Test
    @DisplayName("모임 참여 실패 - 닉네임 중복")
    void joinGroupFailDuplicateNicknameTest() {
        // Given
        String userId = "new-user";
        Long groupId = 1L;
        GroupJoinRequest request = new GroupJoinRequest("중복닉네임", "BLUE");

        User user = User.builder().userId(userId).build();
        Group group = Group.builder()
                .groupName("닉네임 중복 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        given(userRepository.findByUserIdAndStatus(userId, UserStatus.ACTIVE))
                .willReturn(Optional.of(user));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.empty());
        given(memberRepository.countByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(2L);
        given(memberRepository.existsByGroup_GroupIdAndNicknameAndStatus(groupId, "중복닉네임", MemberStatus.ACTIVE))
                .willReturn(true);

        // When & Then
        assertThatThrownBy(() -> groupService.joinGroup(userId, groupId, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_NICKNAME);
    }

    @Test
    @DisplayName("모임 시작 성공 테스트")
    void startGroupSuccessTest() {
        // Given
        Long groupId = 1L;
        String userId = "leader-user";

        Group group = Group.builder()
                .groupName("시작 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member leader = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("리더")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();
        leader.selectBook(1L);

        Book book = Book.builder()
                .title("테스트 책")
                .author("테스트 저자")
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(leader));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(leader));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book));

        // When
        groupService.startGroup(groupId, userId);

        // Then
        assertThat(group.getStatus()).isEqualTo(GroupStatus.IN_PROGRESS);
        verify(assignmentService).createInitialAssignments(groupId, group.getReadingPeriod());
    }

    @Test
    @DisplayName("모임 시작 실패 - 권한 없음")
    void startGroupFailNotLeaderTest() {
        // Given
        Long groupId = 1L;
        String userId = "member-user";

        Group group = Group.builder()
                .groupName("권한 테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("멤버")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));

        // When & Then
        assertThatThrownBy(() -> groupService.startGroup(groupId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_GROUP_OWNER);
    }

    @Test
    @DisplayName("모임 시작 실패 - 이미 시작된 모임")
    void startGroupFailAlreadyStartedTest() {
        // Given
        Long groupId = 1L;
        String userId = "leader-user";

        Group group = Group.builder()
                .groupName("이미 시작된 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();
        group.start(); // 이미 시작됨

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));

        // When & Then
        assertThatThrownBy(() -> groupService.startGroup(groupId, userId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.GROUP_ALREADY_STARTED);
    }
}
