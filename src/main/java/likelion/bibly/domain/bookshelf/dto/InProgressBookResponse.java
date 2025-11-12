package likelion.bibly.domain.bookshelf.dto;

import lombok.Getter;

@Getter
public class InProgressBookResponse {
    private String title;
    private String coverImageUrl;
    private String currentReaderName; // 교환독서 아닐 시 null
    private Long sessionId; // 상세보기 및 다시읽기용 ID

    public InProgressBookResponse(String title, String cover, String reader, Long id) {
        this.title = title;
        this.coverImageUrl = cover;
        this.currentReaderName = reader;
        this.sessionId = id;
    }
}