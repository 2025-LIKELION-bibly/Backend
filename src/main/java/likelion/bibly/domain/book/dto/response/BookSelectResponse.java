package likelion.bibly.domain.book.dto.response;

import java.util.List;

import lombok.Getter;

/**
 * D.2.2 책 선택 완료 응답 DTO
 * 책 선택 후 모든 모임원 정보와 각자 선택한 책 정보를 반환
 */
@Getter
public class BookSelectResponse {
    private Long selectedMemberId;     // 방금 선택한 모임원 ID
    private Long selectedBookId;        // 방금 선택한 책 ID
    private String selectedBookTitle;   // 방금 선택한 책 제목
    private String message;             // 완료 메시지
    private List<MemberBookInfo> members; // 모든 모임원 목록 + 각자 선택한 책

    public BookSelectResponse(Long memberId, Long bookId, String bookTitle, List<MemberBookInfo> members) {
        this.selectedMemberId = memberId;
        this.selectedBookId = bookId;
        this.selectedBookTitle = bookTitle;
        this.message = bookTitle + "을(를) 교환 책으로 선택했습니다.";
        this.members = members;
    }
}
