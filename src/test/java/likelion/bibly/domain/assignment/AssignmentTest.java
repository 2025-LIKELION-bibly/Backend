package likelion.bibly.domain.assignment;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * ReadingAssignment 엔티티 단위 테스트
 * 엔티티의 핵심 비즈니스 로직을 검증합니다.
 */
class AssignmentTest {

    @Test
    @DisplayName("ReadingAssignment 엔티티 빌더 생성 테스트")
    void createReadingAssignmentTest() {
        // Given
        Book book = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .genre("Fiction")
                .pageCount(300)
                .build();

        Group group = Group.builder()
                .groupName("Test Group")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("test-user")
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.LEADER)
                .build();

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(14);

        // When
        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Then
        assertThat(assignment).isNotNull();
        assertThat(assignment.getBook()).isEqualTo(book);
        assertThat(assignment.getGroup()).isEqualTo(group);
        assertThat(assignment.getMember()).isEqualTo(member);
        assertThat(assignment.getCycleNumber()).isEqualTo(1);
        assertThat(assignment.getStartDate()).isNotNull();
        assertThat(assignment.getEndDate()).isNotNull();
        assertThat(assignment.getCreatedAt()).isNotNull();
        assertThat(assignment.getReview()).isNull();
    }

    @Test
    @DisplayName("ReadingAssignment 리뷰 작성 기능 테스트")
    void writeReviewTest() {
        // Given
        Book book = Book.builder()
                .title("Review Test Book")
                .author("Review Author")
                .genre("Non-Fiction")
                .pageCount(250)
                .build();

        Group group = Group.builder()
                .groupName("Review Group")
                .readingPeriod(10)
                .inviteCode("5678")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("reviewer")
                .nickname("리뷰어")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();

        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .build();

        // When
        String review = "정말 좋은 책이었습니다!";
        assignment.writeReview(review);

        // Then
        assertThat(assignment.getReview()).isEqualTo(review);
    }

    @Test
    @DisplayName("ReadingAssignment 종료일 업데이트 기능 테스트")
    void updateEndDateTest() {
        // Given
        Book book = Book.builder()
                .title("Date Test Book")
                .author("Date Author")
                .genre("Fiction")
                .pageCount(200)
                .build();

        Group group = Group.builder()
                .groupName("Date Group")
                .readingPeriod(7)
                .inviteCode("9012")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("date-tester")
                .nickname("날짜테스터")
                .color("GREEN")
                .role(MemberRole.LEADER)
                .build();

        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(7);

        ReadingAssignment assignment = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // When
        LocalDateTime newEndDate = startDate.plusDays(14);
        assignment.updateEndDate(newEndDate);

        // Then
        assertThat(assignment.getEndDate()).isEqualTo(newEndDate);
    }

    @Test
    @DisplayName("ReadingAssignment 리뷰 길이 검증 테스트")
    void reviewLengthTest() {
        // Given
        Book book = Book.builder()
                .title("Length Test Book")
                .author("Length Author")
                .genre("Fiction")
                .pageCount(300)
                .build();

        Group group = Group.builder()
                .groupName("Length Group")
                .readingPeriod(14)
                .inviteCode("1111")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("length-user")
                .nickname("리뷰길이")
                .color("ORANGE")
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

        // When
        String validReview = "이 책은 정말 좋았어요!"; // 40자 이하
        assignment.writeReview(validReview);

        // Then
        assertThat(assignment.getReview()).isEqualTo(validReview);
        assertThat(assignment.getReview().length()).isLessThanOrEqualTo(40);
    }

    @Test
    @DisplayName("ReadingAssignment 여러 회차 테스트")
    void multipleCyclesTest() {
        // Given
        Book book = Book.builder()
                .title("Cycle Test Book")
                .author("Cycle Author")
                .genre("Fiction")
                .pageCount(280)
                .build();

        Group group = Group.builder()
                .groupName("Cycle Group")
                .readingPeriod(10)
                .inviteCode("2222")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("cycle-user")
                .nickname("회차테스터")
                .color("PURPLE")
                .role(MemberRole.LEADER)
                .build();

        // When
        ReadingAssignment cycle1 = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(1)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .build();

        ReadingAssignment cycle2 = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(2)
                .startDate(LocalDateTime.now().plusDays(10))
                .endDate(LocalDateTime.now().plusDays(20))
                .build();

        ReadingAssignment cycle3 = ReadingAssignment.builder()
                .book(book)
                .group(group)
                .member(member)
                .cycleNumber(3)
                .startDate(LocalDateTime.now().plusDays(20))
                .endDate(LocalDateTime.now().plusDays(30))
                .build();

        // Then
        assertThat(cycle1.getCycleNumber()).isEqualTo(1);
        assertThat(cycle2.getCycleNumber()).isEqualTo(2);
        assertThat(cycle3.getCycleNumber()).isEqualTo(3);
    }

    @Test
    @DisplayName("ReadingAssignment 리뷰 작성 후 업데이트 테스트")
    void reviewUpdateTest() {
        // Given
        Book book = Book.builder()
                .title("Update Test Book")
                .author("Update Author")
                .genre("Fiction")
                .pageCount(300)
                .build();

        Group group = Group.builder()
                .groupName("Update Group")
                .readingPeriod(14)
                .inviteCode("3333")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId("update-user")
                .nickname("업데이트")
                .color("YELLOW")
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

        // When
        assignment.writeReview("첫 번째 리뷰");
        String firstReview = assignment.getReview();

        assignment.writeReview("수정된 리뷰");
        String updatedReview = assignment.getReview();

        // Then
        assertThat(firstReview).isNotEqualTo(updatedReview);
        assertThat(updatedReview).isEqualTo("수정된 리뷰");
    }
}
