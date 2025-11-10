package likelion.bibly.domain.bookshelf.service;

import likelion.bibly.domain.bookshelf.entity.BookShelf;
import likelion.bibly.domain.bookshelf.repository.BookShelfRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookShelfService {

    private final BookShelfRepository bookShelfRepository;

    /** 책장 화면: 특정 그룹의 책장 조회 */
    public List<BookShelf> getBookshelfByGroup(Long groupId) {
        // TODO: 그룹 ID를 통해 책장 조회하는 비즈니스 로직 구현
        return bookShelfRepository.findByGroup_GroupId(groupId);
    }

    // TODO: BookShelf 관련 쓰기 로직 추가
}