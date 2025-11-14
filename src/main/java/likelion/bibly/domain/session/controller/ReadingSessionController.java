package likelion.bibly.domain.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import likelion.bibly.domain.session.dto.BookmarkUpdateRequest;
import likelion.bibly.domain.session.dto.ModeChangeRequest;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.dto.ReadingSessionStartRequest;
import likelion.bibly.domain.session.service.ReadingSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions")
@Tag(name = "ReadingSession", description = "F. 책 읽기 세션 API")
public class ReadingSessionController {

    private final ReadingSessionService readingSessionService;

    // (Path: /api/v1/sessions)

    @PostMapping
    @Operation(summary = "독서 세션 시작", description = "새로운 책 읽기 세션을 생성하고 시작 (최초 진입 시)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "세션 생성 성공", content = @Content(schema = @Schema(implementation = ReadingSessionResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자, 책 또는 모임원 정보를 찾을 수 없음")
    })
    public ResponseEntity<ReadingSessionResponse> startSession(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "세션 시작에 필요한 정보")
            @RequestBody ReadingSessionStartRequest request) {

        ReadingSessionResponse response = readingSessionService.startNewReadingSession(
                request.userId(),
                request.bookId(),
                request.memberId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (Path: /api/v1/sessions/user/{userId})
    @GetMapping("/user/{userId}")
    @Operation(summary = "진행 중인 세션 조회", description = "특정 사용자의 현재 진행 중(IN_PROGRESS)인 모든 독서 세션을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<ReadingSessionResponse>> getOngoingSessions(
            @Parameter(description = "현재 사용자 UUID", example = "6a923adb-7096-4e11-9844-dd30177e763a")
            @PathVariable String userId) {

        List<ReadingSessionResponse> responses = readingSessionService.getOngoingSessionsForUser(userId);
        return ResponseEntity.ok(responses);
    }

    // (Path: /api/v1/sessions/{sessionId}/mode)
    @PatchMapping("/{sessionId}/mode")
    @Operation(summary = "모드 전환", description = "특정 세션의 읽기 모드를 변경 (FOCUS <-> TOGETHER)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "모드 변경 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<ReadingSessionResponse> changeMode(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "변경할 모드 정보")
            @RequestBody ModeChangeRequest request) {

        ReadingSessionResponse response = readingSessionService.changeReadingMode(sessionId, request.newMode());
        return ResponseEntity.ok(response);
    }

    // (Path: /api/v1/sessions/{sessionId}/bookmark)
    @PatchMapping("/{sessionId}/bookmark")
    @Operation(summary = "북마크 저장", description = "특정 세션의 북마크(현재 페이지)를 업데이트")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 업데이트 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<ReadingSessionResponse> updateBookmark(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable String sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "새로운 페이지 번호")
            @RequestBody BookmarkUpdateRequest request) {

        ReadingSessionResponse response = readingSessionService.updateBookMark(sessionId, request.pageNumber());
        return ResponseEntity.ok(response);
    }
}