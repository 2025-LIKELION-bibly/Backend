package likelion.bibly.domain.bookshelf.dto;

import likelion.bibly.domain.book.dto.BookSimpleResponse;
import lombok.Data;

import java.util.List;

// [책장 탭 응답 DTO]
@Data
public class BookShelfResponse {
    private int totalBookCount;
    private List<BookSimpleResponse> books;
    private Long groupId;
    private String comment;
    private String sessionId;
    private String userId;

    private List<InProgressBookResponse> inProgressBooks;
    private List<CompletedBookResponse> completedBooks;

    public BookShelfResponse(List<BookSimpleResponse> books, Long groupId, String comment, String sessionId, String userId,
                             List<InProgressBookResponse> inProgress, List<CompletedBookResponse> completed) {
        this.books = books;
        this.totalBookCount = (books != null) ? books.size() : 0;

        this.groupId = groupId;
        this.comment = comment;
        this.sessionId = sessionId;
        this.userId = userId;

        this.inProgressBooks = inProgress;
        this.completedBooks = completed;
    }

    // books 리스트만으로 생성할 수 있도록 임시 생성자 오버로딩 (필요 시)
    //    이 경우 groupId, comment 등은 null
    public BookShelfResponse(List<BookSimpleResponse> books) {
        this.books = books;
        this.totalBookCount = (books != null) ? books.size() : 0;
        // 나머지 필드는 null로 초기화됨
    }
}

