package likelion.bibly.domain.bookshelf.dto;

import lombok.Getter;

@Getter
public class BookInfoResponse {
    private String title;
    private String author;
    private Long bookId;
    private String coverUrl;

    public BookInfoResponse(String title, String author, Long bookId, String coverUrl) {
        this.bookId = bookId;
        this.coverUrl = coverUrl;
        this.title = title;
        this.author = author;
    }
}