package likelion.bibly.domain.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.home.dto.HomeResponse;
import likelion.bibly.domain.home.service.HomeService;
import likelion.bibly.global.exception.BusinessException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home", description = "홈 화면 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/home")
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    @Operation(summary = "홈 화면 데이터 조회", description = "로그인된 사용자 기준으로, 선택된 모임의 모든 홈 화면 정보(책, 멤버, 흔적, 북마크)를 조회")
    public ResponseEntity<HomeResponse> getHomeData(
            @Parameter(description = "현재 로그인된 멤버 ID", required = true, example = "10")
            @RequestParam Long memberId,

            @Parameter(description = "현재 선택된 모임 ID", required = true, example = "1")
            @RequestParam Long groupId) {

        try {
            HomeResponse response = homeService.getHomeData(memberId, groupId);
            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            return ResponseEntity.status(e.getErrorCode().getStatus()).build();

        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        } catch (Exception e) {
            // 그 외 일반 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}