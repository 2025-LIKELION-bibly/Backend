package likelion.bibly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BiblyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiblyApplication.class, args);

	}

}





////이 밑으로 책 DB 구성용(1)...더 하단에 페이지만 분할하는 코드o
//
//package likelion.bibly;
//
//import likelion.bibly.domain.book.entity.Book;
//import likelion.bibly.domain.book.repository.BookRepository;
//import likelion.bibly.domain.book.service.BookService;
//import likelion.bibly.domain.book.service.GutendexService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ComponentScan;
//
//import java.util.List;
//
//@SpringBootApplication
//@ComponentScan(basePackages = {"likelion.bibly"})
//public class BiblyApplication implements CommandLineRunner {
//
//    private final GutendexService gutendexService;
//    private final BookService bookService;
//    private final BookRepository bookRepository;
//
//    public BiblyApplication(GutendexService gutendexService, BookService bookService, BookRepository bookRepository) {
//        this.gutendexService = gutendexService;
//        this.bookService = bookService;
//        this.bookRepository = bookRepository;
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(BiblyApplication.class, args);
//    }
//
//    /**
//     * 애플리케이션 시작 직후 한 번 실행: 40권 원문 및 페이지 분할 내용 저장
//     */
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- Starting Gutendex Data Import ---");
//
//        // GutendexService 호출: 책 메타데이터 및 원문 저장
//        gutendexService.fetchAndSaveBooksWithContent(40);
//
//        System.out.println("--- Starting Page Content Splitting ---");
//
//        // 모든 책에 대해 페이지 분할 메서드 실행
//        List<Book> allBooks = bookRepository.findAll();
//
//        for (Book book : allBooks) {
//            try {
//                // BookService의 분할 및 저장 메서드 호출
//                bookService.saveBookContentAsPages(book.getBookId());
//                System.out.println("Book ID " + book.getBookId() + " (" + book.getTitle() + ") processed.");
//            } catch (Exception e) {
//                System.err.println("Error processing Book ID " + book.getBookId() + ": " + e.getMessage());
//                // 에러 발생 시 로그를 남기고 다음 책으로 이동
//            }
//        }
//
//        System.out.println("--- Data Import and Splitting Finished ---");
//    }
//}



// 이 밑으로 페이지 분할용(2)(원문 새로 받아오지 않고 기존 데이터로 분할)
//package likelion.bibly;
//
//import likelion.bibly.domain.book.entity.Book;
//import likelion.bibly.domain.book.repository.BookRepository;
//import likelion.bibly.domain.book.service.BookService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ComponentScan;
//
//import java.util.List;
//
//@SpringBootApplication
//@ComponentScan(basePackages = {"likelion.bibly"})
//public class BiblyApplication implements CommandLineRunner {
//
//    private final BookService bookService;
//    private final BookRepository bookRepository;
//
//    public BiblyApplication(
//            BookService bookService,
//            BookRepository bookRepository) {
//        this.bookService = bookService;
//        this.bookRepository = bookRepository;
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(BiblyApplication.class, args);
//    }
//
//    /**
//     * 애플리케이션 시작 직후, 기존 DB에 있는 모든 책의 원문을 페이지로 분할하여 저장합니다.
//     */
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- Starting Existing Book Content Splitting ---");
//
//        // 1. DB에 저장된 모든 책 조회
//        List<Book> allBooks = bookRepository.findAll();
//
//        if (allBooks.isEmpty()) {
//            System.out.println("No books found in the database. Splitting skipped.");
//            return;
//        }
//
//        System.out.println(allBooks.size() + " books found. Starting page segmentation.");
//
//        // 2. 모든 책에 대해 페이지 분할 메서드 실행
//        for (Book book : allBooks) {
//            // 원문이 비어있지 않은 책만 처리
//            if (book.getContent() != null && !book.getContent().isEmpty()) {
//                try {
//                    // BookService의 분할 및 저장 메서드 호출
//                    bookService.saveBookContentAsPages(book.getBookId());
//                    System.out.println("✅ Book ID " + book.getBookId() + " (" + book.getTitle() + ") processed and pages saved.");
//                } catch (Exception e) {
//                    System.err.println("❌ Error processing Book ID " + book.getBookId() + ": " + e.getMessage());
//                    // 에러 발생 시 로그를 남기고 다음 책으로 이동
//                }
//            } else {
//                System.out.println("⚠️ Book ID " + book.getBookId() + " has no content. Skipping.");
//            }
//        }
//
//        System.out.println("--- Page Content Splitting Finished ---");
//    }
//}
