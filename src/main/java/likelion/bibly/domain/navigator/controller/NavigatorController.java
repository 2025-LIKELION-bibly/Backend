package likelion.bibly.domain.navigator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.bookshelf.entity.BookShelf;
import likelion.bibly.domain.bookshelf.service.BookShelfService;
import likelion.bibly.domain.navigator.enums.CurrentTab;
import likelion.bibly.domain.navigator.service.NavigatorService;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// TODO: Swagger 코드 처리

@Tag(name = "Navigator", description = "네비게이터: 탭 이동 및 각 탭의 초기 데이터 로딩 API")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class NavigatorController {

    private final NavigatorService navigatorService;
    private final ReadingSessionService readingSessionService;
    private final BookShelfService bookShelfService;
    // TODO: 홈 서비스 작성

    private final Long TEMP_MEMBER_ID = 1L;
    private final Long TEMP_GROUP_ID = 1L;

    /** 1. 홈 화면 탭 (Path: /api/v1/home) */
    @Operation(summary = "홈 화면", description = "네비게이터 CurrentTab을 HOME으로 업데이트, 홈 화면 데이터 반환")
    @GetMapping("/home")
    public ResponseEntity<String> getHomeData() {
        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.HOME);

        // TODO: 홈 엔터티 작성
        String summary = "환영합니다! 홈 화면 데이터";

        return ResponseEntity.ok(summary);
    }

    /** 2. 책읽기 화면 탭 (Path: /api/v1/reading-sessions) */
    @Operation(summary = "책읽기 화면 데이터 조회", description = "네비게이터 CurrentTab을 READING_SESSION으로 업데이트, 현재 진행 중인 독서 세션 목록을 반환") 
    @GetMapping("/reading-sessions")
    public ResponseEntity<List<ReadingSession>> getReadingSessions() {
        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.READING_SESSION);

        List<ReadingSession> sessions = readingSessionService.getOngoingSessionsForMember(TEMP_MEMBER_ID);

        return ResponseEntity.ok(sessions);
    }

    /** 3. 책장 화면 탭 (Path: /api/v1/bookshelf) */
    @Operation(summary = "책장 화면 데이터 조회", description = "네비게이터 CurrentTab을 BOOKSHELF로 업데이트, 지정된 그룹의 책장을 반환")
    @GetMapping("/bookshelf")
    public ResponseEntity<List<BookShelf>> getBookshelf(
            @Parameter(description = "조회할 책장 그룹 ID (기본값: 1)", example = "1") // ★ Swagger/OpenAPI
            @RequestParam(required = false, defaultValue = "1") Long groupId) {

        navigatorService.updateCurrentTab(TEMP_MEMBER_ID, CurrentTab.BOOKSHELF);

        List<BookShelf> bookshelf = bookShelfService.getBookshelfByGroup(groupId);

        return ResponseEntity.ok(bookshelf);
    }
}