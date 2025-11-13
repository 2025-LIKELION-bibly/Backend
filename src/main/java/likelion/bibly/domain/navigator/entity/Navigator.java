package likelion.bibly.domain.navigator.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.navigator.enums.CurrentTab;
import likelion.bibly.domain.user.entity.User;
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
    @Column(name = "user_id",length = 40)
    private String userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // user_id (PK): Member 엔터티 ID와 공유 (FK 매핑)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "current_tab")
    private CurrentTab currentTab;

    @Builder
    public Navigator(User user, CurrentTab currentTab) {
        this.user = user;
        this.currentTab = currentTab;
    }

    // 탭 변경(
    public void updateCurrentTab(CurrentTab currentTab) {
        this.currentTab = currentTab;
    }
}