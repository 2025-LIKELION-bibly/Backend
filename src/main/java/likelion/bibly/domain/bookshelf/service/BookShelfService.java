package likelion.bibly.domain.bookshelf.service;

import likelion.bibly.domain.book.dto.BookSimpleResponse;
import likelion.bibly.domain.bookshelf.dto.BookShelfResponse;
import likelion.bibly.domain.bookshelf.entity.BookShelf;
import likelion.bibly.domain.bookshelf.repository.BookShelfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookShelfService {

    private final BookShelfRepository bookShelfRepository;

    /** 책장 화면: 특정 그룹의 책장 조회 */
    // 반환 타입을 DTO로 변경
    public BookShelfResponse getBookshelfByGroup(Long groupId) {

        // DB에서 엔티티 리스트 조회
        List<BookShelf> bookShelves = bookShelfRepository.findByGroup_GroupId(groupId);

        List<BookSimpleResponse> bookDtos = bookShelves.stream()
                .map(bookShelf -> new BookSimpleResponse(bookShelf.getBook()))
                .collect(Collectors.toList());

        return new BookShelfResponse(bookDtos);
    }

    // TODO: BookShelf 관련 쓰기 로직 추가
}