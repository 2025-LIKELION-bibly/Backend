package likelion.bibly.domain.book.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class GutendexResponse {
    private int count;
    private String next;
    private String previous;
    private List<BookDto> results;

    @Data
    @NoArgsConstructor
    public static class BookDto {
        private Long id;
        private String title;
        private List<AuthorDto> authors;
        private List<String> subjects; // summaries 대신 subjects를 사용 (Gutendex JSON 구조 기준)
        private List<String> bookshelves;
        private Map<String, String> formats; // MIME-Type: URL 맵
        private int download_count;
    }

    @Data
    @NoArgsConstructor
    public static class AuthorDto {
        private Integer birth_year;
        private Integer death_year;
        private String name;
    }
}
