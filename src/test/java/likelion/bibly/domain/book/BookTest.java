package likelion.bibly.domain.book;

import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.book.service.BookService;
import likelion.bibly.domain.book.dto.response.BookDetailResponse;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class BookTest {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    @DisplayName("Book 엔티티 생성 테스트")
    void createBookTest() {
        // Given
        Book book = Book.builder()
                .title("Test Book")
                .author("Test Author")
                .genre("Fiction")
                .publishedAt(LocalDateTime.now())
                .publisher("Test Publisher")
                .isbn("1234567890")
                .pageCount(300)
                .description("Test Description")
                .coverUrl("http://example.com/cover.jpg")
                .content("Test Content")
                .build();

        // When
        Book savedBook = bookRepository.save(book);

        // Then
        assertThat(savedBook).isNotNull();
        assertThat(savedBook.getBookId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Test Book");
        assertThat(savedBook.getAuthor()).isEqualTo("Test Author");
        assertThat(savedBook.getPopularityScore()).isEqualTo(0);
        assertThat(savedBook.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Book 인기도 증가 테스트")
    void increasePopularityTest() {
        // Given
        Book book = Book.builder()
                .title("Popular Book")
                .author("Popular Author")
                .genre("Fiction")
                .pageCount(200)
                .build();
        Book savedBook = bookRepository.save(book);

        // When
        savedBook.increasePopularity();
        savedBook.increasePopularity();
        savedBook.increasePopularity();

        // Then
        assertThat(savedBook.getPopularityScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("새로 나온 책 목록 조회 테스트")
    void getNewBooksTest() {
        // Given
        for (int i = 1; i <= 5; i++) {
            Book book = Book.builder()
                    .title("New Book " + i)
                    .author("Author " + i)
                    .genre("Fiction")
                    .pageCount(200)
                    .build();
            bookRepository.save(book);
        }

        // When
        List<BookSimpleResponse> newBooks = bookService.getNewBooks();

        // Then
        assertThat(newBooks).isNotEmpty();
        assertThat(newBooks.size()).isLessThanOrEqualTo(20);
    }

    @Test
    @DisplayName("인기 있는 책 목록 조회 테스트")
    void getPopularBooksTest() {
        // Given
        Book popularBook = Book.builder()
                .title("Most Popular Book")
                .author("Popular Author")
                .genre("Fiction")
                .pageCount(300)
                .build();
        Book savedBook = bookRepository.save(popularBook);

        // 인기도 증가
        for (int i = 0; i < 10; i++) {
            savedBook.increasePopularity();
        }

        // When
        List<BookSimpleResponse> popularBooks = bookService.getPopularBooks();

        // Then
        assertThat(popularBooks).isNotEmpty();
    }

    @Test
    @DisplayName("책 상세 정보 조회 테스트")
    void getBookDetailTest() {
        // Given
        Book book = Book.builder()
                .title("Detail Test Book")
                .author("Detail Author")
                .genre("Non-Fiction")
                .description("Detailed description")
                .pageCount(400)
                .build();
        Book savedBook = bookRepository.save(book);

        // When
        BookDetailResponse response = bookService.getBookDetail(savedBook.getBookId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isEqualTo(savedBook.getBookId());
        assertThat(response.getTitle()).isEqualTo("Detail Test Book");
        assertThat(response.getAuthor()).isEqualTo("Detail Author");
    }

    @Test
    @DisplayName("존재하지 않는 책 조회 시 예외 발생 테스트")
    void getBookDetail_NotFound_Test() {
        // Given
        Long nonExistentBookId = 999999L;

        // When & Then
        assertThatThrownBy(() -> bookService.getBookDetail(nonExistentBookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("Book 조회 및 업데이트 테스트")
    void findAndUpdateBookTest() {
        // Given
        Book book = Book.builder()
                .title("Original Title")
                .author("Original Author")
                .genre("Fiction")
                .pageCount(250)
                .build();
        Book savedBook = bookRepository.save(book);

        // When
        Book foundBook = bookRepository.findById(savedBook.getBookId()).orElseThrow();
        foundBook.increasePopularity();

        // Then
        assertThat(foundBook.getPopularityScore()).isEqualTo(1);
        assertThat(foundBook.getTitle()).isEqualTo("Original Title");
    }
}
