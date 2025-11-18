package likelion.bibly.domain.book.dto.request;

/**
 * D.2.2 책 선택 요청 DTO
 */
public record BookSelectRequest(
        Long memberId  // 책을 선택하는 모임원 ID
) {
}
