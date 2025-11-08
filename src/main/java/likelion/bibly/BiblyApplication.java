package likelion.bibly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BiblyApplication {

	public static void main(String[] args) {
		SpringApplication.run(BiblyApplication.class, args);

	}

}





//이 밑으로 책 DB 구성용

//package likelion.bibly;
//
//import likelion.bibly.domain.book.service.GutendexService;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.ComponentScan;
//
//@SpringBootApplication
//@ComponentScan(basePackages = {"likelion.bibly"})
//public class BiblyApplication implements CommandLineRunner {
//
//    private final GutendexService gutendexService;
//
//    public BiblyApplication(GutendexService gutendexService) {
//        this.gutendexService = gutendexService;
//    }
//
//    public static void main(String[] args) {
//        SpringApplication.run(BiblyApplication.class, args);
//    }
//
//    /**
//     * 애플리케이션 시작 직후 한 번 실행
//     */
//    @Override
//    public void run(String... args) throws Exception {
//        System.out.println("--- Starting Gutendex Data Import ---");
//        // GutendexService 호출
//        gutendexService.fetchAndSaveBooksWithContent(40);
//        System.out.println("--- Data Import Finished ---");
//    }
//}