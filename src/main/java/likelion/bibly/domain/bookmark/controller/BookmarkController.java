package likelion.bibly.domain.bookmark.controller;

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
import likelion.bibly.domain.bookmark.service.BookmarkService;
import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@Tag(name = "Bookmark", description = "북마크 생성, 조회, 진행도 갱신")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/sessions/{sessionId}/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    // POST /api/v1/sessions/{sessionId}/bookmarks
    @PostMapping
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

        BookmarkResponse response = bookmarkService.saveNewBookmark(
                sessionId,
                request.pageNumber(),
                memberId
        );

        return ResponseEntity.ok(response);
    }

    // POST /api/v1/sessions/{sessionId}/bookmarks/update
    @PostMapping("/update")
    @Operation(summary = "단일 북마크 업데이트", description = "페이지를 넘길 때 최신 페이지를 단일값으로 저장(진행도 갱신)")
    public ResponseEntity<ReadingSessionResponse> updateBookmark(
            @PathVariable Long sessionId,
            @RequestBody BookmarkUpdateRequest request
    ) {
        Long memberId = request.memberId();
        Integer pageNumber = request.pageNumber();

        try {
            // readingSessionService 대신 bookmarkService 사용
            ReadingSessionResponse response = bookmarkService.updateBookMark(
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
            // Progress 엔티티가 누락된 경우 등 내부 서버 오류 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // GET /api/v1/sessions/{sessionId}/bookmarks
    @GetMapping
    @Operation(summary = "북마크 목록 조회", description = "특정 세션에 기록된 멤버의 북마크 이력 목록을 최신순으로 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "북마크 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음")
    })
    public ResponseEntity<List<BookmarkListResponse>> getBookmarks(
            @Parameter(description = "세션 ID", example = "1")
            @PathVariable Long sessionId,

            @Parameter(description = "요청 멤버 ID ", example = "10")
            @RequestParam Long memberId) {

        List<BookmarkListResponse> response = bookmarkService.getBookmarksBySessionAndMember(sessionId, memberId);

        return ResponseEntity.ok(response);
    }
}