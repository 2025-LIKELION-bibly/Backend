package likelion.bibly.domain.addgroup.entity;

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
import likelion.bibly.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "add_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AddGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "add_id")
    private Long addId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private Member createdBy;

    @Column(name = "new_group_name", length = 100)
    private String newGroupName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Builder
    public AddGroup(Member createdBy, String newGroupName) {
        this.createdBy = createdBy;
        this.newGroupName = newGroupName;
        this.createdAt = LocalDateTime.now();
    }

    // 모임명 업데이트 메서드 (필요하다면 추가)
    public void updateNewGroupName(String newGroupName) {
        this.newGroupName = newGroupName;
    }
}