package likelion.bibly.domain.book.service;

/**
 * Gutendex API 연동 서비스 인터페이스
 */
public interface GutendexService {

	/**
	 * Gutendex API에서 책 데이터를 가져와 DB에 저장
	 * @param limit 저장할 최대 책의 개수
	 */
	void fetchAndSaveBooksWithContent(int limit) throws Exception;
}
