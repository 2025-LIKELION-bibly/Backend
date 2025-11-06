package likelion.bibly.domain.assignment.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reading_assignment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReadingAssignment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "selection_id")
	private Long selectionId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "book_id", nullable = false)
	private Book book;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(name = "cycle_number", nullable = false)
	private Integer cycleNumber;

	@Column(name = "start_date", nullable = false)
	private LocalDateTime startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDateTime endDate;

	@Column(name = "review", length = 40)
	private String review;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Builder
	public ReadingAssignment(Book book, Group group, Member member, Integer cycleNumber,
		LocalDateTime startDate, LocalDateTime endDate) {
		this.book = book;
		this.group = group;
		this.member = member;
		this.cycleNumber = cycleNumber;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdAt = LocalDateTime.now();
	}

	public void writeReview(String review) {
		this.review = review;
	}
}
