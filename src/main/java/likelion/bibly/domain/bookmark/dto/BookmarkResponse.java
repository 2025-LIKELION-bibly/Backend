package likelion.bibly.domain.bookmark.dto;

import likelion.bibly.domain.bookmark.entity.Bookmark;
import java.time.LocalDateTime;

public record BookmarkResponse(
        Long bookmarkId,
        Long sessionId,
        Long memberId,
        Integer bookMarkPage,
        LocalDateTime createdAt
) {

    public BookmarkResponse(Bookmark bookmark) {
        this(
                bookmark.getBookmarkId(),
                bookmark.getSession().getSessionId(),
                bookmark.getMember().getMemberId(),
                bookmark.getBookMarkPage(),
                bookmark.getCreatedAt()
        );
    }
}