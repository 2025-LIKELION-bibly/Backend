package likelion.bibly.domain.highlight.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.highlight.dto.HighlightCreateRequest;
import likelion.bibly.domain.highlight.dto.HighlightResponse;
import likelion.bibly.domain.highlight.service.HighlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/highlights")
@Tag(name = "Highlight", description = "하이라이트(형광펜) API")
public class HighlightController {

    private final HighlightService highlightService;

    /**
     * H.1 하이라이트 생성
     */
    //path: /api/v1/highlights
    @PostMapping
    @Operation(summary = "하이라이트 생성",
            description = "사용자가 선택한 범위에 하이라이트 생성")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = HighlightResponse.class))),
            @ApiResponse(responseCode = "404", description = "세션 또는 멤버를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "유효하지 않은 오프셋 또는 페이지 정보")
    })
    public ResponseEntity<HighlightResponse> createHighlight(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "하이라이트 생성 정보 (색상, 위치 오프셋 포함)")
            @RequestBody HighlightCreateRequest request) {

        HighlightResponse response = highlightService.createHighlight(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---

    /**
     * H.2 하이라이트 삭제
     */
    //path: /api/v1/highlights/{highlightId}
    @DeleteMapping("/{highlightId}")
    @Operation(summary = "하이라이트 삭제",
            description = "특정 하이라이트를 삭제. 코멘트/메모가 연결되어 있으면 삭제 불가.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "하이라이트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "코멘트/메모가 연결되어 삭제할 수 없음 (Conflict)")
    })
    public ResponseEntity<Void> deleteHighlight(
            @Parameter(description = "삭제할 하이라이트 ID", example = "5")
            @PathVariable Long highlightId,
            @Parameter(description = "요청한 멤버 ID", example = "1")
            @RequestParam Long memberId) throws AccessDeniedException {

        // 삭제 로직 수행 및 연결된 코멘트/메모 존재 여부 검사
        highlightService.deleteHighlight(highlightId, memberId);

        return ResponseEntity.noContent().build();
    }
}