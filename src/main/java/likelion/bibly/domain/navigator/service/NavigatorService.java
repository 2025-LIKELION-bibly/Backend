// src/main/java/likelion/bibly/domain/navigator/service/NavigatorService.java
package likelion.bibly.domain.navigator.service; // ★ 경로 수정

import likelion.bibly.domain.navigator.entity.Navigator;
import likelion.bibly.domain.navigator.enums.CurrentTab;
import likelion.bibly.domain.navigator.repository.NavigatorRepository;
import likelion.bibly.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NavigatorService {

    private final NavigatorRepository navigatorRepository;

    public CurrentTab getCurrentTab(String userId) {
        Navigator navigator = navigatorRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Navigator Not Found for User ID: " + userId));

        return navigator.getCurrentTab();
    }

    @Transactional
    public void updateCurrentTab(String userId, CurrentTab newTab) {
        Navigator navigator = navigatorRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Navigator Not Found for User ID: " + userId));

        // Navigator 엔터티의 updateCurrentTab 메서드를 호출하여 상태 변경
        navigator.updateCurrentTab(newTab);
    }

    @Transactional
    public Navigator createDefaultNavigator(User user) {
        Navigator newNavigator = Navigator.builder()
                .user(user)
                .currentTab(CurrentTab.HOME)
                .build();
        return navigatorRepository.save(newNavigator);
    }
}