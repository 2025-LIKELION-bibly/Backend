package likelion.bibly.domain.book.service;

import likelion.bibly.domain.book.dto.GutendexResponse;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GutendexService {

    private final BookRepository bookRepository;
    // RestTemplate 초기화 시 리다이렉트가 명시적으로 활성화된 팩토리 사용
    private final RestTemplate restTemplate = createConfiguredRestTemplate();
    // 인기순으로 호출
    private static final String BASE_URL = "https://gutendex.com/books?languages=en&sort=popular";


    private static RestTemplate createConfiguredRestTemplate() {
        // 1. 요청 설정 정의: 리다이렉트 활성화 (RequestConfig 사용)
        RequestConfig config = RequestConfig.custom()
                .setRedirectsEnabled(true)
                .build();

        // 2. HttpClient 5.x 빌더를 사용하여 클라이언트 인스턴스 생성
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(config)
                .build();

        // HttpComponentsClientHttpRequestFactory를 사용하여 RestTemplate에 연결
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        // 3. 타임아웃 설정 추가 (RestTemplate 레벨)
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(15000);

        return new RestTemplate(factory);
    }

    /**
     * @param limit 저장할 최대 책의 개수
     */
    @Transactional
    public void fetchAndSaveBooksWithContent(int limit) throws Exception {
        String nextUrl = BASE_URL;
        int savedCount = 0;

        // 로컬 저장 폴더 준비
        File dir = new File(System.getProperty("user.dir") + "/src/main/resources/books");
        if (!dir.exists()) dir.mkdirs();

        // 페이지네이션 루프 (limit 개수만큼 저장될 때까지 반복)
        while (nextUrl != null && savedCount < limit) {
            System.out.println("Fetching page: " + nextUrl);

            GutendexResponse response = restTemplate.getForObject(nextUrl, GutendexResponse.class);

            if (response == null || response.getResults() == null) {
                break;
            }

            for (GutendexResponse.BookDto bookDto : response.getResults()) {
                if (savedCount >= limit) break;

                // 데이터 가공
                String authors = bookDto.getAuthors().stream()
                        .map(GutendexResponse.AuthorDto::getName)
                        .collect(Collectors.joining(", "));

                String summaries = String.join("\n", bookDto.getSubjects());
                String bookshelves = String.join(", ", bookDto.getBookshelves());
                if (bookshelves.isEmpty()) bookshelves = "Unknown";

                // 저자 생년 기준으로 publishedAt 임의 설정
                int birthYear = bookDto.getAuthors().isEmpty() || bookDto.getAuthors().get(0).getBirth_year() == null
                        ? 1900 : bookDto.getAuthors().get(0).getBirth_year();
                LocalDateTime publishedAt = LocalDateTime.of(birthYear, 1, 1, 0, 0);

                // text/plain 원문 URL 찾기 및 다운로드 (리다이렉트 자동 처리)
                String contentUrl = findTextContentUrl(bookDto.getFormats());
                String content = null;

                if (contentUrl != null) {
                    try {
                        // Apache HttpClient가 리다이렉션을 따라가서 원문 텍스트를 가져옴
                        content = restTemplate.getForObject(contentUrl, String.class);
                    } catch (Exception e) {
                        System.err.println("Content download failed for ID " + bookDto.getId() + ": " + e.getMessage());
                        content = "Content download failed.";
                    }
                }

                // 3. 엔티티 생성 및 DB 저장 (사용자님의 Book 엔티티 구조에 맞춤)
                Book book = Book.builder()
                        .title(bookDto.getTitle())
                        .author(authors)
                        .genre(bookshelves)
                        .publishedAt(publishedAt)
                        .publisher("Gutenberg")
                        .isbn(null)
                        .pageCount(null)
                        .description(summaries.substring(0, Math.min(summaries.length(), 255)))
                        .coverUrl(null)
                        .content(content)
                        .build();

                bookRepository.save(book);
                savedCount++;
            }

            nextUrl = response.getNext(); // 다음 페이지로 이동
            if (savedCount >= limit) break;

            // API 요청 간 딜레이 추가 (필요 시)
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(savedCount + " books saved to DB");
        System.out.println("Saved to: " + dir.getAbsolutePath());
    }

    /**
     * Gutendex 포맷 맵에서 text/plain 파일을 찾는 유틸
     */
    private String findTextContentUrl(Map<String, String> formats) {
        if (formats.containsKey("text/plain")) {
            return formats.get("text/plain");
        }
        if (formats.containsKey("text/plain; charset=utf-8")) {
            return formats.get("text/plain; charset=utf-8");
        }

        return formats.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("text/plain"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }
}