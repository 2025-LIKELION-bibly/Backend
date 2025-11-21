package likelion.bibly.domain.book;

import likelion.bibly.domain.book.entity.Book;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Book 엔티티 단위 테스트
 * 엔티티의 핵심 비즈니스 로직을 검증합니다.
 */
class BookTest {

    @Test
    @DisplayName("Book 엔티티 빌더 생성 테스트")
    void createBookWithBuilderTest() {
        // Given
        String title = "Test Book";
        String author = "Test Author";
        String genre = "Fiction";
        Integer pageCount = 300;

        // When
        Book book = Book.builder()
                .title(title)
                .author(author)
                .genre(genre)
                .pageCount(pageCount)
                .build();

        // Then
        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo(title);
        assertThat(book.getAuthor()).isEqualTo(author);
        assertThat(book.getGenre()).isEqualTo(genre);
        assertThat(book.getPageCount()).isEqualTo(pageCount);
        assertThat(book.getPopularityScore()).isEqualTo(0);
        assertThat(book.getCreatedAt()).isNotNull();
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

        // When
        book.increasePopularity();
        book.increasePopularity();
        book.increasePopularity();

        // Then
        assertThat(book.getPopularityScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("Book 생성 시 기본값 검증")
    void bookDefaultValuesTest() {
        // Given & When
        Book book = Book.builder()
                .title("Default Test Book")
                .author("Default Author")
                .genre("Non-Fiction")
                .pageCount(250)
                .build();

        // Then - 기본값 확인
        assertThat(book.getPopularityScore()).isEqualTo(0);
        assertThat(book.getCreatedAt()).isNotNull();
        assertThat(book.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Book 모든 속성 설정 테스트")
    void createBookWithAllPropertiesTest() {
        // Given
        LocalDateTime publishedAt = LocalDateTime.of(2020, 1, 1, 0, 0);
        String publisher = "Test Publisher";
        String isbn = "1234567890";
        String description = "Test Description";
        String coverUrl = "http://example.com/cover.jpg";
        String content = "Test Content";

        // When
        Book book = Book.builder()
                .title("Complete Book")
                .author("Complete Author")
                .genre("Fiction")
                .publishedAt(publishedAt)
                .publisher(publisher)
                .isbn(isbn)
                .pageCount(400)
                .description(description)
                .coverUrl(coverUrl)
                .content(content)
                .build();

        // Then
        assertThat(book.getTitle()).isEqualTo("Complete Book");
        assertThat(book.getAuthor()).isEqualTo("Complete Author");
        assertThat(book.getGenre()).isEqualTo("Fiction");
        assertThat(book.getPublishedAt()).isEqualTo(publishedAt);
        assertThat(book.getPublisher()).isEqualTo(publisher);
        assertThat(book.getIsbn()).isEqualTo(isbn);
        assertThat(book.getPageCount()).isEqualTo(400);
        assertThat(book.getDescription()).isEqualTo(description);
        assertThat(book.getCoverUrl()).isEqualTo(coverUrl);
        assertThat(book.getContent()).isEqualTo(content);
        assertThat(book.getPopularityScore()).isEqualTo(0);
        assertThat(book.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Book 인기도 순차적 증가 검증")
    void popularityIncrementalTest() {
        // Given
        Book book = Book.builder()
                .title("Incremental Book")
                .author("Incremental Author")
                .genre("Fiction")
                .pageCount(300)
                .build();

        // Then - 초기 인기도 0
        assertThat(book.getPopularityScore()).isEqualTo(0);

        // When & Then - 순차적 증가
        book.increasePopularity();
        assertThat(book.getPopularityScore()).isEqualTo(1);

        book.increasePopularity();
        assertThat(book.getPopularityScore()).isEqualTo(2);

        book.increasePopularity();
        assertThat(book.getPopularityScore()).isEqualTo(3);
    }

    @Test
    @DisplayName("여러 Book 엔티티 생성 테스트")
    void multipleBooksTest() {
        // Given & When
        Book book1 = Book.builder()
                .title("Book 1")
                .author("Author 1")
                .genre("Fiction")
                .pageCount(200)
                .build();

        Book book2 = Book.builder()
                .title("Book 2")
                .author("Author 2")
                .genre("Non-Fiction")
                .pageCount(300)
                .build();

        Book book3 = Book.builder()
                .title("Book 3")
                .author("Author 3")
                .genre("Science")
                .pageCount(400)
                .build();

        // Then
        assertThat(book1.getTitle()).isEqualTo("Book 1");
        assertThat(book2.getTitle()).isEqualTo("Book 2");
        assertThat(book3.getTitle()).isEqualTo("Book 3");

        assertThat(book1.getPopularityScore()).isEqualTo(0);
        assertThat(book2.getPopularityScore()).isEqualTo(0);
        assertThat(book3.getPopularityScore()).isEqualTo(0);

        assertThat(book1.getCreatedAt()).isNotNull();
        assertThat(book2.getCreatedAt()).isNotNull();
        assertThat(book3.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Book 인기도 대량 증가 테스트")
    void massivePopularityIncreaseTest() {
        // Given
        Book book = Book.builder()
                .title("Very Popular Book")
                .author("Popular Author")
                .genre("Fiction")
                .pageCount(500)
                .build();

        // When - 10번 증가
        for (int i = 0; i < 10; i++) {
            book.increasePopularity();
        }

        // Then
        assertThat(book.getPopularityScore()).isEqualTo(10);
    }

    @Test
    @DisplayName("Book 필수 속성만으로 생성 테스트")
    void createBookWithMinimalPropertiesTest() {
        // Given & When
        Book book = Book.builder()
                .title("Minimal Book")
                .author("Minimal Author")
                .build();

        // Then
        assertThat(book.getTitle()).isEqualTo("Minimal Book");
        assertThat(book.getAuthor()).isEqualTo("Minimal Author");
        assertThat(book.getPopularityScore()).isEqualTo(0);
        assertThat(book.getCreatedAt()).isNotNull();
        // Optional fields are null
        assertThat(book.getGenre()).isNull();
        assertThat(book.getPageCount()).isNull();
        assertThat(book.getPublisher()).isNull();
    }
}
