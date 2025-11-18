package likelion.bibly.domain.book.dto.response;

import likelion.bibly.domain.book.entity.Book;
import lombok.Getter;

// 네비게이터 구현용 중복 응답값 분리
@Getter
public class BookSimpleResponse {
    private Long bookId;
    private String title;
    private String author;
    private String coverImageUrl;
    
    // 생성자
    public BookSimpleResponse(Book book) {
        this.bookId= book.getBookId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.coverImageUrl = book.getCoverUrl();
        
    }
}
