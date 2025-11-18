package likelion.bibly.domain.book.dto.response;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.member.entity.Member;
import lombok.Getter;

/**
 * D.2.2 책 선택 완료 시 반환되는 모임원 정보
 * (모임원 이름 + 선택한 책 정보)
 */
@Getter
public class MemberBookInfo {
    private Long memberId;
    private String nickname;
    private String color;
    private Long bookId;           // 선택한 책 ID (선택 안 했으면 null)
    private String bookTitle;      // 선택한 책 제목 (선택 안 했으면 null)
    private String bookCoverUrl;   // 선택한 책 이미지 (선택 안 했으면 null)

    public MemberBookInfo(Member member, Book selectedBook) {
        this.memberId = member.getMemberId();
        this.nickname = member.getNickname();
        this.color = member.getColor();

        if (selectedBook != null) {
            this.bookId = selectedBook.getBookId();
            this.bookTitle = selectedBook.getTitle();
            this.bookCoverUrl = selectedBook.getCoverUrl();
        }
    }
}
