package likelion.bibly.domain.bookmark.dto;

import java.util.List;

public record BookmarkListResponse(
        List<BookmarkResponse> bookmarks
) {
    public static BookmarkListResponse of(List<BookmarkResponse> bookmarks) {
        return new BookmarkListResponse(bookmarks);
    }
}