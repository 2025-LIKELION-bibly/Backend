package likelion.bibly.domain.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "북마크(페이지) 업데이트 요청 DTO")
public record BookmarkUpdateRequest(
        @Schema(description = "업데이트할 페이지 번호", example = "120")
        Integer pageNumber
) {}
