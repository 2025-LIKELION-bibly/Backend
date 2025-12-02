package likelion.bibly.domain.assignment.service;

import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.assignment.dto.response.NextReadingBookResponse;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * AssignmentService 단위 테스트
 * Service 계층의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class AssignmentServiceTest {

    @InjectMocks
    private AssignmentServiceImpl assignmentService;

    @Mock
    private ReadingAssignmentRepository assignmentRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private ProgressRepository progressRepository;

    @Test
    @DisplayName("한줄평 등록 성공 테스트")
    void writeReviewSuccessTest() {
        // Given
        Long assignmentId = 1L;
        String userId = "test-user";
        String review = "정말 좋은 책이었습니다!";

        Book book = Book.builder()
                .title("테스트 책")
                .author("테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(14))
                .build();

        given(assignmentRepository.findById(assignmentId)).willReturn(Optional.of(assignment));

        // When
        AssignmentResponse response = assignmentService.writeReview(assignmentId, userId, review);

        // Then
        assertThat(response).isNotNull();
        assertThat(assignment.getReview()).isEqualTo(review);
        verify(assignmentRepository).findById(assignmentId);
    }

    @Test
    @DisplayName("한줄평 등록 실패 - 권한 없음")
    void writeReviewFailAccessDeniedTest() {
        // Given
        Long assignmentId = 1L;
        String userId = "wrong-user";
        String review = "리뷰 내용";

        Book book = Book.builder()
                .title("테스트 책")
                .author("테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("other-user") // 다른 사용자
                .nickname("다른사람")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(14))
                .build();

        given(assignmentRepository.findById(assignmentId)).willReturn(Optional.of(assignment));

        // When & Then
        assertThatThrownBy(() -> assignmentService.writeReview(assignmentId, userId, review))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("현재 배정 조회 성공 테스트")
    void getCurrentAssignmentSuccessTest() {
        // Given
        String userId = "test-user";
        Long groupId = 1L;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        Book book = Book.builder()
                .title("현재 읽는 책")
                .author("테스트 저자")
                .build();

        LocalDateTime now = LocalDateTime.now();
        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(13))
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of(assignment));

        // When
        AssignmentResponse response = assignmentService.getCurrentAssignment(userId, groupId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookTitle()).isEqualTo("현재 읽는 책");
        verify(groupRepository).findById(groupId);
        verify(memberRepository).findByGroup_GroupIdAndUserId(groupId, userId);
    }

    @Test
    @DisplayName("현재 배정 조회 실패 - 배정 없음")
    void getCurrentAssignmentFailNotFoundTest() {
        // Given
        String userId = "test-user";
        Long groupId = 1L;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of()); // 배정 없음

        // When & Then
        assertThatThrownBy(() -> assignmentService.getCurrentAssignment(userId, groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ASSIGNMENT_NOT_FOUND);
    }

    @Test
    @DisplayName("현재 회차의 모든 배정 조회 성공 테스트")
    void getCurrentAssignmentsSuccessTest() {
        // Given
        Long groupId = 1L;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member1 = Member.builder()
                .group(group)
                .userId("user1")
                .nickname("멤버1")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();
        member1.selectBook(1L);

        Member member2 = Member.builder()
                .group(group)
                .userId("user2")
                .nickname("멤버2")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();
        member2.selectBook(2L);

        Book book1 = Book.builder().title("책1").author("저자1").build();
        Book book2 = Book.builder().title("책2").author("저자2").build();

        LocalDateTime now = LocalDateTime.now();
        ReadingAssignment assignment1 = ReadingAssignment.builder()
                .book(book1)
                .group(group)
                .member(member1)
                .cycleNumber(1)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(13))
                .build();

        ReadingAssignment assignment2 = ReadingAssignment.builder()
                .book(book2)
                .group(group)
                .member(member2)
                .cycleNumber(1)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(13))
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(member1, member2));
        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of(assignment1, assignment2));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book1));
        given(bookRepository.findById(2L)).willReturn(Optional.of(book2));

        // When
        CurrentAssignmentResponse response = assignmentService.getCurrentAssignments(groupId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getGroupName()).isEqualTo("테스트 모임");
        assertThat(response.getCurrentCycle()).isEqualTo(1);
        assertThat(response.getMemberAssignments()).hasSize(2);
        verify(groupRepository).findById(groupId);
    }

    @Test
    @DisplayName("초기 배정 생성 성공 테스트")
    void createInitialAssignmentsSuccessTest() {
        // Given
        Long groupId = 1L;
        Integer readingPeriod = 14;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(readingPeriod)
                .inviteCode("1234")
                .build();

        Member member1 = Member.builder()
                .group(group)
                .userId("user1")
                .nickname("멤버1")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();
        member1.selectBook(1L);

        Member member2 = Member.builder()
                .group(group)
                .userId("user2")
                .nickname("멤버2")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();
        member2.selectBook(2L);

        Book book1 = Book.builder().title("책1").author("저자1").build();
        Book book2 = Book.builder().title("책2").author("저자2").build();

        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(member1, member2));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book1));
        given(bookRepository.findById(2L)).willReturn(Optional.of(book2));

        // When
        assignmentService.createInitialAssignments(groupId, readingPeriod);

        // Then
        verify(memberRepository).findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
        verify(assignmentRepository, times(4)).save(any(ReadingAssignment.class)); // 2명 * 2회차
    }

    @Test
    @DisplayName("추가 배정 생성 성공 테스트")
    void createAdditionalAssignmentsSuccessTest() {
        // Given
        Long groupId = 1L;
        Integer readingPeriod = 14;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(readingPeriod)
                .inviteCode("1234")
                .build();

        Member member1 = Member.builder()
                .group(group)
                .userId("user1")
                .nickname("멤버1")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();
        member1.selectBook(1L);

        Member member2 = Member.builder()
                .group(group)
                .userId("user2")
                .nickname("멤버2")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();
        member2.selectBook(2L);

        Book book1 = Book.builder().title("책1").author("저자1").build();
        Book book2 = Book.builder().title("책2").author("저자2").build();

        LocalDateTime baseDate = LocalDateTime.now();
        ReadingAssignment existingAssignment = ReadingAssignment.builder()
                .book(book1)
                .group(group)
                .member(member1)
                .cycleNumber(2)
                .startDate(baseDate.minusDays(14))
                .endDate(baseDate)
                .build();

        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of(existingAssignment));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(member1, member2));
        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(bookRepository.findById(1L)).willReturn(Optional.of(book1));
        given(bookRepository.findById(2L)).willReturn(Optional.of(book2));

        // When
        assignmentService.createAdditionalAssignments(groupId, readingPeriod);

        // Then
        verify(assignmentRepository).findByGroup_GroupId(groupId);
        verify(assignmentRepository, times(4)).save(any(ReadingAssignment.class)); // 2명 * 2회차
    }

    @Test
    @DisplayName("현재 읽고 있는 책 정보 조회 성공 테스트")
    void getCurrentReadingBookSuccessTest() {
        // Given
        String userId = "test-user";
        Long groupId = 1L;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        Book book = Book.builder()
                .title("현재 읽는 책")
                .author("테스트 저자")
                .pageCount(300)
                .build();

        LocalDateTime now = LocalDateTime.now();
        ReadingAssignment currentAssignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(13))
                .build();

        Progress progress = Progress.builder()
                .book(book)
                .member(member)
                .currentPage(50)
                .progress(0.167f) // 16.7%
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of(currentAssignment));
        given(progressRepository.findByMemberAndBook(member, book))
                .willReturn(Optional.of(progress));

        // When
        CurrentReadingBookResponse response = assignmentService.getCurrentReadingBook(userId, groupId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isEqualTo(book.getBookId());
        assertThat(response.getCurrentPage()).isEqualTo(50);
        assertThat(response.getProgressPercent()).isEqualTo(17.0f); // getCurrentProgressPercentage()는 int를 반환하므로 반올림됨
        verify(groupRepository).findById(groupId);
    }

    @Test
    @DisplayName("다음에 읽을 책 정보 조회 성공 테스트")
    void getNextReadingBookSuccessTest() {
        // Given
        String userId = "test-user";
        Long groupId = 1L;

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        Book currentBook = Book.builder()
                .title("현재 책")
                .author("저자1")
                .build();

        Book nextBook = Book.builder()
                .title("다음 책")
                .author("저자2")
                .genre("Fiction")
                .description("다음 책 설명")
                .build();

        LocalDateTime now = LocalDateTime.now();
        ReadingAssignment currentAssignment = ReadingAssignment.builder()
                .book(currentBook)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(now.minusDays(1))
                .endDate(now.plusDays(13))
                .build();

        ReadingAssignment nextAssignment = ReadingAssignment.builder()
                .book(nextBook)
                .group(group)
                .member(member)
                .cycleNumber(2)
                .startDate(now.plusDays(13))
                .endDate(now.plusDays(27))
                .build();

        given(groupRepository.findById(groupId)).willReturn(Optional.of(group));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(assignmentRepository.findByGroup_GroupId(groupId))
                .willReturn(List.of(currentAssignment, nextAssignment));

        // When
        NextReadingBookResponse response = assignmentService.getNextReadingBook(userId, groupId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookTitle()).isEqualTo("다음 책");
        assertThat(response.getAuthor()).isEqualTo("저자2");
        assertThat(response.getGenre()).isEqualTo("Fiction");
        verify(groupRepository).findById(groupId);
    }
}
