package likelion.bibly.domain.bookshelf.dto;

import lombok.Getter;

@Getter
public class CompletedBookResponse {
    private String title;
    private String coverImageUrl;
    private Long sessionId; // 상세보기 및 다시읽기용 ID

    public CompletedBookResponse(String title, String cover, Long id) {
        this.title = title;
        this.coverImageUrl = cover;
        this.sessionId = id;
    }
}