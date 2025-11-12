package likelion.bibly.domain.session.dto;

import likelion.bibly.domain.book.dto.BookSimpleResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import lombok.Data;

import java.time.LocalDateTime;

// [책읽기 탭 응답 DTO]
@Data
public class ReadingSessionResponse {
    private Long readingSessionId;
    private String isCurrentSession;
    private String mode;
    private int currentPage;
    private int totalPages;
    private LocalDateTime lastReadTime;

    // 공통 DTO 재사용
    private BookSimpleResponse bookInfo;

    public ReadingSessionResponse(ReadingSession session) {
        // Todo: 생성자 추후 구체화
    }
}