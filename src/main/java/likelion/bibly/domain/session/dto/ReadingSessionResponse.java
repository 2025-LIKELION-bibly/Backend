package likelion.bibly.domain.session.dto;

import likelion.bibly.domain.book.dto.BookSimpleResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import lombok.Data;

import java.util.List;

// [책읽기 탭 응답 DTO]
@Data
public class ReadingSessionResponse {
    private String sessionId;
    private String isCurrentSession;
    private String mode;
    private String bookId;
    private int currentPage;
    private int totalPages;

    // 공통 DTO 재사용
    private BookSimpleResponse bookInfo;

    public ReadingSessionResponse(ReadingSession session, List<BookSimpleResponse> books, String sessionId) {
        this.sessionId = sessionId;
        this.isCurrentSession = session.getIsCurrentSession() != null ? session.getIsCurrentSession().toString() : null; // 혹은 "false" / "true"로 적절히 변환
        this.bookId = books.getFirst().toString();
        this.mode = session.getMode() != null ? session.getMode().toString() : null; // Enum이라면 String으로 변환

        this.currentPage = session.getBookMark();
        this.totalPages = session.getBook().getPageCount();

        if (session.getBook() != null) {
            this.bookInfo = new BookSimpleResponse(session.getBook());
        }
    }
}