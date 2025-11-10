package likelion.bibly.domain.navigator.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.navigator.enums.CurrentTab;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "navigator")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Navigator {

    // 사용자 ID (FK, PK)
    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // user_id (PK): Member 엔터티 ID와 공유 (FK 매핑)
    @JoinColumn(name = "user_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_tab")
    private CurrentTab currentTab;

    @Builder
    public Navigator(Member member, CurrentTab currentTab) {
        this.member = member;
        this.userId = member.getMemberId();
        this.currentTab = currentTab;
    }

    // 탭 변경(
    public void updateCurrentTab(CurrentTab currentTab) {
        this.currentTab = currentTab;
    }
}