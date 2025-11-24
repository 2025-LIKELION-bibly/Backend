package likelion.bibly.domain.session.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.bookmark.dto.BookmarkListResponse;
import likelion.bibly.domain.bookmark.dto.BookmarkResponse;
import likelion.bibly.domain.bookmark.dto.BookmarkUpdateRequest;
import likelion.bibly.domain.session.dto.ModeChangeRequest;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.dto.ReadingSessionStartRequest;
import likelion.bibly.domain.session.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
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

                request.bookId(),
                request.memberId(),
                request.groupId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // (Path: /api/v1/sessions/member/{memberId})
    @GetMapping("/member/{memberId}")
    @Operation(summary = "진행 중인 세션 조회", description = "특정 사용자의 현재 진행 중(IN_PROGRESS)인 모든 독서 세션을 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    public ResponseEntity<List<ReadingSessionResponse>> getOngoingSessions(
            @Parameter(description = "멤버 Id", example = "1")
            @PathVariable Long memberId) {

        List<ReadingSessionResponse> responses = readingSessionService.getOngoingSessionsForMember(memberId);
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
            @PathVariable Long sessionId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "변경할 모드 정보")
            @RequestBody ModeChangeRequest request) {

        ReadingSessionResponse response = readingSessionService.changeReadingMode(sessionId, request.newMode());
        return ResponseEntity.ok(response);
    }

    // (Path: /api/v1/sessions/{sessionId}/bookmarks)

    @PostMapping("/{sessionId}/bookmarks")
    @Operation(summary = "북마크 이력 생성", description = "특정 세션에 새로운 북마크(페이지) 이력을 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 생성 및 진행도 갱신 성공",
                    content = @Content(schema = @Schema(implementation = BookmarkResponse.class))),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<BookmarkResponse> createBookmark(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable Long sessionId,

            @Parameter(description = "요청 멤버 ID", example = "10")
            @RequestParam Long memberId,

            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "현재 읽고 있는 페이지 번호")
            @RequestBody BookmarkUpdateRequest request) {

        BookmarkResponse response = readingSessionService.saveNewBookmark(
                sessionId,
                request.pageNumber(),
                memberId
        );

        return ResponseEntity.ok(response);
    }

    // (Path: /api/v1/sessions/{sessionId}/bookmarks/update)
    @PostMapping("/{sessionId}/bookmarks/update")
    @Operation(summary = "단일 북마크 업데이트", description = "페이지를 넘길 때 최신 페이지를 단일값으로 저장(진행도 갱신)")
    public ResponseEntity<ReadingSessionResponse> updateBookmark(
            @PathVariable Long sessionId,
            @RequestBody BookmarkUpdateRequest request
    ) {
        Long memberId = request.memberId();
        Integer pageNumber = request.pageNumber();

        try {
            ReadingSessionResponse response = readingSessionService.updateBookMark(
                    sessionId,
                    memberId,
                    pageNumber
            );
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // (Path: /api/v1/sessions/{sessionId}/bookmarks)
    @GetMapping("/{sessionId}/bookmarks")
    @Operation(summary = "북마크 목록 조회", description = "특정 세션에 기록된 멤버의 북마크 이력 목록을 최신순으로 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<BookmarkListResponse> getBookmarks(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable Long sessionId,

            @Parameter(description = "요청 멤버 ID ", example = "10")
            @RequestParam Long memberId) {

        BookmarkListResponse response = readingSessionService.getBookmarksBySessionAndMember(sessionId, memberId);

        return ResponseEntity.ok(response);
    }

    // (Path: /api/v1/sessions/{sessionId}/finish)
    @PatchMapping("/{sessionId}/finish")
    @Operation(summary = "세션 종료",
            description = """
        독서 세션을 종료하고 완료 상태(COMPLETED)로 변경
        - 세션은 기본적으로 기간 만료 시 자동 종료됩니다.
        - 필요할 경우 수동으로 종료시킬 수 있습니다.
        
        """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세션 종료 성공"),
            @ApiResponse(responseCode = "400", description = "이미 종료된 세션이거나 조건 불충족"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "세션 소유 권한 없음")
    })
    public ResponseEntity<ReadingSessionResponse> finishSession(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable Long sessionId,

            @Parameter(description = "요청 멤버 ID", example = "10")
            @RequestParam Long memberId) {

        ReadingSessionResponse response = readingSessionService.finishReadingSession(sessionId, memberId);
        return ResponseEntity.ok(response);
    }
}