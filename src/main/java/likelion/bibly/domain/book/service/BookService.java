package likelion.bibly.domain.book.service;

import java.util.List;

import likelion.bibly.domain.book.dto.response.BookDetailResponse;
import likelion.bibly.domain.book.dto.response.BookSelectResponse;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;

/**
 * 책 관리 서비스 인터페이스
 */
public interface BookService {

	/**
	 * D.1.1 새로 나온 책 목록 조회
	 */
	List<BookSimpleResponse> getNewBooks();

	/**
	 * D.1.2 지금 많이 읽는 책 (인기 있는 책) 목록 조회
	 */
	List<BookSimpleResponse> getPopularBooks();

	/**
	 * D.2.1 책 상세 정보 조회
	 * @param bookId 조회할 책 ID
	 * @return 책 상세 정보
	 */
	BookDetailResponse getBookDetail(Long bookId);

	/**
	 * D.2.2 책 선택 (교환 책으로 선택)
	 * @param bookId 선택할 책 ID
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 선택 완료 응답 (모든 모임원 + 각자 선택한 책 정보)
	 */
	BookSelectResponse selectBook(Long bookId, String userId, Long groupId);

	/**
	 * D.3.1 책의 원문을 페이지 단위로 분할하여 DB에 저장
	 * 이 메서드는 책 등록 시 호출
	 * @param bookId 원문을 분할할 책의 ID
	 */
	void saveBookContentAsPages(Long bookId);

	/**
	 * 특정 책의 특정 페이지 내용을 조회(프론트 조회용)
	 * @param bookId 책 ID
	 * @param pageNumber 요청된 페이지 번호
	 * @return 해당 페이지의 텍스트 내용
	 */
	String getPageContent(Long bookId, Integer pageNumber);
}
