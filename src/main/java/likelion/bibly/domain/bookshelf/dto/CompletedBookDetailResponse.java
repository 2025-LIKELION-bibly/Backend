package likelion.bibly.domain.bookshelf.dto;

import likelion.bibly.domain.highlight.dto.HighlightResponse;
import lombok.Getter;

import java.util.List;

@Getter
public class CompletedBookDetailResponse {
    private BookInfoResponse bookInfo;
    private Integer bookMarkPage;
    private List<HighlightResponse> highlights; // '흔적' (하이라이트 + 코멘트)

    public CompletedBookDetailResponse(BookInfoResponse bookInfo, Integer page, List<HighlightResponse> highlights) {
        this.bookInfo = bookInfo;
        this.bookMarkPage = page;
        this.highlights = highlights;
    }
}