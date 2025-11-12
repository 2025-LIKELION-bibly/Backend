package likelion.bibly.domain.navigator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.bookshelf.dto.BookShelfResponse;
import likelion.bibly.domain.bookshelf.service.BookShelfService;
import likelion.bibly.domain.home.dto.HomeResponse;
import likelion.bibly.domain.home.service.HomeService;
import likelion.bibly.domain.navigator.enums.CurrentTab;
import likelion.bibly.domain.navigator.service.NavigatorService;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.service.ReadingSessionService;
// import likelion.bibly.domain.bookshelf.entity.BookShelf;
// import likelion.bibly.domain.session.entity.ReadingSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Navigator", description = "B 네비게이터 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NavigatorController {

    private final NavigatorService navigatorService;
    private final ReadingSessionService readingSessionService;
    private final BookShelfService bookShelfService;
    private final HomeService homeService;

    // TODO: 추후 실제 사용자 ID 가져오도록 수정
    private final Long TEMP_MEMBER_ID = 1L;

    /** 1. 홈 화면 탭 (Path: /api/v1/home) */
    @Operation(summary = "홈 탭 이동", description = "홈 탭으로 이동, 홈 화면에 필요한 데이터 반환")
    @GetMapping("/home")
    public ResponseEntity<HomeResponse> getHomeData() { // 반환 타입을 DTO
        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.HOME);

        HomeResponse homeData = homeService.getHomeData(TEMP_MEMBER_ID);

        return ResponseEntity.ok(homeData);
    }

    /** 2. 책읽기 화면 탭 (Path: /api/v1/reading-sessions) */
    @Operation(summary = "책읽기 탭 이동", description = "책읽기 탭으로 이동, 진행 중이던 독서 세션 정보 반환")
    @GetMapping("/reading-sessions")
    public ResponseEntity<List<ReadingSessionResponse>> getReadingSessions() { // 7. 반환 타입 DTO 리스트로
        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.READING_SESSION);

        List<ReadingSessionResponse> sessions = readingSessionService.getOngoingSessionsForMember(TEMP_MEMBER_ID);

        return ResponseEntity.ok(sessions);
    }

    /** 3. 책장 화면 탭 (Path: /api/v1/bookshelf) */
    @Operation(summary = "책장 탭 이동", description = "책장 탭으로 이동, 지정된 그룹의 책장을 반환")
    @GetMapping("/bookshelf")
    public ResponseEntity<BookShelfResponse> getBookshelf( // 반환 타입 책장 DTO
                                                              @Parameter(description = "조회할 책장 그룹 ID (기본값: 1)", example = "1")
                                                              @RequestParam(required = false, defaultValue = "1") Long groupId,
                                                                            Long currentUserId) {

        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.BOOKSHELF);

        BookShelfResponse bookshelf = bookShelfService.getBookshelfByGroup(groupId, currentUserId);

        return ResponseEntity.ok(bookshelf);
    }
}