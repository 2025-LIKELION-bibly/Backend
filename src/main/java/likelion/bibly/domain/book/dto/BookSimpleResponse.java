package likelion.bibly.domain.book.dto;

import likelion.bibly.domain.book.entity.Book;

// 네비게이터 구현용 중복 응답값 분리
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
