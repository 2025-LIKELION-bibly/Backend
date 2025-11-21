package likelion.bibly.domain.page.entity;

import jakarta.persistence.*;
import likelion.bibly.domain.book.entity.Book;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "page_content")
public class PageContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false) // FK 컬럼 이름
    private Book book;

    // 페이지 순서 (1부터 시작, 이 필드를 기준으로 조회)
    @Column(name = "page_number", nullable = false)
    private Integer pageNumber;

    // 페이지 내용
    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

}