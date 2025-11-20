package likelion.bibly.domain.comment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import likelion.bibly.domain.comment.dto.CommentCreateRequest;
import likelion.bibly.domain.comment.dto.CommentResponse;
import likelion.bibly.domain.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
@Tag(name = "Comment/Memo", description = "코멘트 및 메모 API (스레드)")
public class CommentController {

    private final CommentService commentService;

    /**
     * G.1 코멘트/메모 생성
     */
    //path: /api/v1/comments
    @PostMapping
    @Operation(summary = "코멘트/메모 생성",
            description = "하이라이트된 구절에 코멘트 또는 메모를 추가. 25자 초과 시 메모로 자동 분류되며, parentCommentId를 포함하면 답글로 생성됨.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공", content = @Content(schema = @Schema(implementation = CommentResponse.class))),
            @ApiResponse(responseCode = "404", description = "필수 엔티티(세션, 멤버, 하이라이트, 부모 코멘트)를 찾을 수 없음")
    })
    public ResponseEntity<CommentResponse> createComment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "코멘트/메모 생성 정보 (하이라이트 ID, 내용, 부모 코멘트 ID 등)")
            @RequestBody CommentCreateRequest request) {

        CommentResponse response = commentService.createComment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---

    /**
     * G.2 코멘트/메모 삭제
     */

    //path: /api/v1/comments/{commentId}
    @DeleteMapping("/{commentId}")
    @Operation(summary = "코멘트/메모 삭제",
            description = "특정 코멘트/메모를 삭제. 답글이 달려있는 코멘트는 삭제 불가.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공 (No Content)"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음 (작성자가 아님)"),
            @ApiResponse(responseCode = "404", description = "코멘트를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "자식 코멘트가 존재하여 삭제할 수 없음 (Conflict)")
    })
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "삭제할 코멘트/메모 ID", example = "10")
            @PathVariable Long commentId,
            @Parameter(description = "요청한 멤버 ID", example = "1")
            @RequestParam Long memberId) throws AccessDeniedException {

        // 삭제 로직 수행 및 자식 코멘트 존재 여부 검사
        commentService.deleteComment(commentId, memberId);

        return ResponseEntity.noContent().build();
    }
}