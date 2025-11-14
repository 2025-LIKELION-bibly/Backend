package likelion.bibly.domain.member.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "member_id")
	private Long memberId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id", nullable = false)
	private Group group;

	@Column(name = "user_id", nullable = false)
	private String userId;

	@Column(name = "selected_book_id")
	private Long selectedBookId;

	@Column(name = "nickname", length = 50, nullable = false)
	private String nickname;

	@Enumerated(EnumType.STRING)
	@Column(name = "role")
	private MemberRole role;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private MemberStatus status;

	@Column(name = "color", length = 20, nullable = false)
	private String color;

	@Column(name = "joined_at")
	private LocalDateTime joinedAt;

	@Column(name = "withdrawn")
	private LocalDateTime withdrawn;

	@Builder
	public Member(Group group, String userId, String nickname, String color, MemberRole role) {
		this.group = group;
		this.userId = userId;
		this.nickname = nickname;
		this.color = color;
		this.role = role;
		this.status = MemberStatus.ACTIVE;
		this.joinedAt = LocalDateTime.now();
	}

	public void selectBook(Long bookId) {
		this.selectedBookId = bookId;
	}

	public void withdraw() {
		this.status = MemberStatus.WITHDRAWN;
		this.withdrawn = LocalDateTime.now();
	}
}
