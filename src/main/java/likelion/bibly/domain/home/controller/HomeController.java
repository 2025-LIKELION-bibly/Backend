package likelion.bibly.domain.home.controller;

import likelion.bibly.domain.home.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

//    /**
//     * E.1 디폴트 홈 화면에 필요한 모든 통합 데이터를 조회합니다.
//     * @param memberId 현재 사용자 ID (인증 정보에서 가져오는 것이 권장되지만, PathVariable로 가정)
//     * @param groupId 현재 선택된 그룹 ID (파라미터가 없으면 가장 마지막 접속 그룹을 사용하도록 Service에서 로직 구현 필요)
//     * @return HomeResponse DTO
//     */
//    @GetMapping("/{memberId}")
//    public ResponseEntity<HomeResponse> getHomeData(
//            @PathVariable Long memberId,
//            @RequestParam(required = false) Long groupId) {
//
//        // groupId가 없을 경우, Service에서 memberId를 기반으로 기본 그룹을 찾도록 구현해야 합니다.
//        if (groupId == null) {
//            // 이 로직은 Service 레이어에서 memberId 기반으로 가장 마지막 접속 그룹을 찾아 ID를 반환하도록 하는 것이 좋습니다.
//            // 여기서는 단순화를 위해 PathVariable로 받은 memberId를 그대로 사용한다고 가정합니다.
//            // 실제 구현 시 groupId를 찾는 로직이 필요합니다.
//            // ==return ResponseEntity.ok(homeService.getHomeDataWithDefaultGroup(memberId));
//        }
//
//        HomeResponse response = homeService.getHomeData(memberId, groupId);
//        return ResponseEntity.ok(response);
//    }
}