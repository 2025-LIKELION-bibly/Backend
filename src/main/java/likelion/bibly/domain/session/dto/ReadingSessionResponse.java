package likelion.bibly.domain.session.dto;

import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import lombok.Data;

// [책읽기 탭 응답 DTO]
@Data
public class ReadingSessionResponse {
    private Long sessionId;
    private String isCurrentSession;
    private String mode;
    private Long bookId;
    private Integer bookMarkPage;
    private int totalPages;

    // 공통 DTO 재사용
    private BookSimpleResponse bookInfo;

    public ReadingSessionResponse(ReadingSession session) {
        this.sessionId = session.getSessionId();
        this.isCurrentSession = session.getIsCurrentSession() != null ? session.getIsCurrentSession().toString() : null;
        this.mode = session.getMode() != null ? session.getMode().toString() : null;

        this.bookId = session.getBook() != null ? session.getBook().getBookId() : null;

        this.bookMarkPage = session.getBookmark() != null ? session.getBookmark().getBookMarkPage() : 0;

        // session.getBook()이 null이 아닌지 확인하고,
        // getPageCount() 결과가 null이면 0을 사용(임시)
        Integer pageCount = session.getBook() != null ? session.getBook().getPageCount() : null;
        this.totalPages = pageCount != null ? pageCount : 0;

        if (session.getBook() != null) {
            this.bookInfo = new BookSimpleResponse(session.getBook());
        }
    }

    public static ReadingSessionResponse from(ReadingSession session, Integer latestBookMarkPage) {

        ReadingSessionResponse response = new ReadingSessionResponse(session);

        response.setSessionId(session.getSessionId());
        response.setIsCurrentSession(session.getIsCurrentSession() != null ? session.getIsCurrentSession().toString() : null);
        response.setMode(session.getMode() != null ? session.getMode().toString() : null);
        response.setBookId(session.getBook() != null ? session.getBook().getBookId() : null);

        // 북마크 페이지 설정
        response.setBookMarkPage(latestBookMarkPage != null ? latestBookMarkPage : 0);

        // totalPages 설정
        Integer pageCount = session.getBook() != null ? session.getBook().getPageCount() : null;
        response.setTotalPages(pageCount != null ? pageCount : 0);

        // BookSimpleResponse 설정
        if (session.getBook() != null) {
            response.setBookInfo(new BookSimpleResponse(session.getBook()));
        }

        return response;
    }
}
