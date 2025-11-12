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
    private Long sessionId;
    private Long memberId;

    public BookShelfResponse(List<BookSimpleResponse> books, Long groupId, String comment, Long sessionId, Long memberId) {
        this.books = books;
        this.totalBookCount = (books != null) ? books.size() : 0;

        this.groupId = groupId;
        this.comment = comment;
        this.sessionId = sessionId;
        this.memberId = memberId;
    }

    // books 리스트만으로 생성할 수 있도록 임시 생성자 오버로딩 (필요 시)
    //    이 경우 groupId, comment 등은 null
    public BookShelfResponse(List<BookSimpleResponse> books) {
        this.books = books;
        this.totalBookCount = (books != null) ? books.size() : 0;
        // 나머지 필드는 null로 초기화됨
    }
}