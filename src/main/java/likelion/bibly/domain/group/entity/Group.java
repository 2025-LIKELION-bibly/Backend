package likelion.bibly.domain.group.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import likelion.bibly.domain.group.enums.GroupStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`group`")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Group {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "group_id")
	private Long groupId;

	@Column(name = "group_name", length = 100)
	private String groupName;

	@Column(name = "reading_period", nullable = false)
	private Integer readingPeriod;

	@Column(name = "invite_code", length = 4, unique = true, nullable = false)
	private String inviteCode;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private GroupStatus status;

	@Column(name = "started_at")
	private LocalDateTime startedAt;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "invite_code_expired_at")
	private LocalDateTime inviteCodeExpiredAt;

	@Builder
	public Group(String groupName, Integer readingPeriod, String inviteCode) {
		this.groupName = groupName;
		this.readingPeriod = readingPeriod;
		this.inviteCode = inviteCode;
		this.status = GroupStatus.WAITING;
		this.createdAt = LocalDateTime.now();
		this.inviteCodeExpiredAt = LocalDateTime.now().plusDays(7);
	}

	public void start() {
		this.status = GroupStatus.IN_PROGRESS;
		this.startedAt = LocalDateTime.now();
	}

	public void complete() {
		this.status = GroupStatus.COMPLETED;
	}

	public void reset() {
		this.status = GroupStatus.WAITING;
		this.startedAt = null;
	}

	public void clearExpiredInviteCode() {
		if (inviteCodeExpiredAt != null && LocalDateTime.now().isAfter(inviteCodeExpiredAt)) {
			this.inviteCode = null;
		}
	}

	public boolean isInviteCodeValid() {
		return inviteCode != null && inviteCodeExpiredAt != null && LocalDateTime.now().isBefore(inviteCodeExpiredAt);
	}
}
