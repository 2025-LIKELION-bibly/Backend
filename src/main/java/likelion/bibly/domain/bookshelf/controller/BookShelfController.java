package likelion.bibly.domain.bookshelf.controller;

import likelion.bibly.domain.bookshelf.dto.BookShelfResponse;
import likelion.bibly.domain.bookshelf.dto.CompletedBookDetailResponse;
import likelion.bibly.domain.highlight.dto.HighlightResponse;
import likelion.bibly.domain.bookshelf.service.BookShelfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "BookShelf", description = "H. 모임별 책장 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/groups/{groupId}/bookshelf")
public class BookShelfController {

    private final BookShelfService bookShelfService;

    /**
     * 책장 메인 화면 조회 (진행 중 / 완료된 책 목록) (Path: /api/v1/groups/{groupId}/bookshelf)
     */
    @Operation(summary = "모임 책장 메인 조회", description = "해당 모임의 '진행 중인 책'과 '완료된 책' 목록 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "책장 조회 성공",
                    content = @Content(schema = @Schema(implementation = BookShelfResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 모임을 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping
    public ResponseEntity<BookShelfResponse> getBookshelf(
            @Parameter(description = "조회할 모임 ID", in = ParameterIn.PATH, required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 사용자의 UUID ID", in = ParameterIn.QUERY, required = true, example = "95c52b78-8aa6-494e-beaa-0c970d257ec5")
            @RequestParam("currentUserId") String currentUserId){


        BookShelfResponse response = bookShelfService.getBookshelfByGroup(groupId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 완료된 책 상세 보기 (Path: /api/v1/groups/{groupId}/bookshelf/completed/{sessionId})
     */
    @Operation(summary = "완료된 책 상세 조회", description = "완료된 책 1권의 상세 정보(책 정보, 북마크, 내 흔적) 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상세 조회 성공",
                    content = @Content(schema = @Schema(implementation = CompletedBookDetailResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션 또는 사용자 정보를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/completed/{sessionId}")
    public ResponseEntity<CompletedBookDetailResponse> getCompletedBookDetails(
            @Parameter(description = "현재 모임 ID", in = ParameterIn.PATH, required = true, example = "1")
            @PathVariable Long groupId,

            @Parameter(description = "상세 조회할 '완료된' 세션(책)의 ID", in = ParameterIn.PATH, required = true, example = "101")
            @PathVariable Long sessionId,
            @Parameter(description = "현재 사용자의 UUID ID", in = ParameterIn.QUERY, required = true, example = "95c52b78-8aa6-494e-beaa-0c970d257ec5")
            @RequestParam("currentUserId") String currentUserId){

        CompletedBookDetailResponse response = bookShelfService.getCompletedBookDetails(sessionId, currentUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * 완료된 책 '다시 읽기' 선택 (Path: /api/v1/groups/{groupId}/bookshelf/completed/{sessionId}/reread)
     */
    @Operation(summary = "완료된 책'다시 읽기' 선택", description = "완료된 책을 '다시 읽기' (새로운 '진행 중' 세션 생성)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "'다시 읽기' 성공. 새로 생성된 '진행 중' 세션 ID 반환",
                    content = @Content(schema = @Schema(implementation = Long.class))),
            @ApiResponse(responseCode = "404", description = "완료된 세션 또는 사용자 정보를 찾을 수 없습니다.", content = @Content)
    })
    @PostMapping("/completed/{sessionId}/reread")
    public ResponseEntity<Long> rereadBook(
            @Parameter(description = "현재 모임 ID", in = ParameterIn.PATH, required = true, example = "1")
            @PathVariable Long groupId,

            @Parameter(description = "다시 읽을 '완료된' 세션(책)의 ID", in = ParameterIn.PATH, required = true, example = "101")
            @PathVariable Long sessionId,
            @Parameter(description = "모임원 ID", in = ParameterIn.QUERY, required = true, example = "95c52b78-8aa6-494e-beaa-0c970d257ec5")
            @RequestParam("memberId") Long memberId){

        Long newSessionId = bookShelfService.rereadBook(sessionId, memberId);
        return ResponseEntity.ok(newSessionId);
    }

    /**
     * 흔적 모아보기 (모임 기준)...홈과 동일 (Path: /api/v1/groups/{groupId}/bookshelf/traces)
     */
    @Operation(summary = "흔적 모아보기", description = "해당 모임의 모든 흔적(하이라이트 + 코멘트) 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "흔적 모아보기 성공",
                    content = @Content(schema = @Schema(implementation = HighlightResponse.class))), // 반환값은 List<HighlightResponse>
            @ApiResponse(responseCode = "404", description = "모임 정보를 찾을 수 없습니다.", content = @Content)
    })
    @GetMapping("/traces")
    public ResponseEntity<List<HighlightResponse>> getTracesForGroup(
            @Parameter(description = "흔적을 모아볼 모임 ID", in = ParameterIn.PATH, required = true, example = "1")
            @PathVariable Long groupId,
            @Parameter(description = "현재 사용자의 UUID ID", in = ParameterIn.QUERY, required = true, example = "95c52b78-8aa6-494e-beaa-0c970d257ec5")
            @RequestParam("currentUserId") String currentUserId){

        List<HighlightResponse> response = bookShelfService.getTracesForGroup(groupId, currentUserId);
        return ResponseEntity.ok(response);
    }
}