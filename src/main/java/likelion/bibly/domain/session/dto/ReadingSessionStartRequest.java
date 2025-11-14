package likelion.bibly.domain.session.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "새 독서 세션 시작 요청 DTO")
public record ReadingSessionStartRequest(
        @Schema(description = "현재 사용자 UUID", example = "6a923adb-7096-4e11-9844-dd30177e763a")
        String userId,
        @Schema(description = "책 ID", example = "1")
        Long bookId,
        @Schema(description = "모임원 ID", example = "1")
        Long memberId
) {}

