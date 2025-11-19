package likelion.bibly.domain.book.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import likelion.bibly.domain.book.dto.response.BookDetailResponse;
import likelion.bibly.domain.book.dto.response.BookSelectResponse;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.book.dto.response.MemberBookInfo;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    /**
     * D.1.1 새로 나온 책 목록 조회
     */
    public List<BookSimpleResponse> getNewBooks() {
        List<Book> books = bookRepository.findTop20ByOrderByCreatedAtDesc();
        return books.stream()
                .map(BookSimpleResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * D.1.2 지금 많이 읽는 책 (인기 있는 책) 목록 조회
     */
    public List<BookSimpleResponse> getPopularBooks() {
        List<Book> books = bookRepository.findTop20ByOrderByPopularityScoreDesc();
        return books.stream()
                .map(BookSimpleResponse::new)
                .collect(Collectors.toList());
    }

    /**
     * D.2.1 책 상세 정보 조회
     * @param bookId 조회할 책 ID
     * @return 책 상세 정보
     */
    public BookDetailResponse getBookDetail(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId));

        return new BookDetailResponse(book);
    }

    /**
     * D.2.2 책 선택 (교환 책으로 선택)
     * @param bookId 선택할 책 ID
     * @param memberId 선택하는 모임원 ID
     * @return 선택 완료 응답 (모든 모임원 + 각자 선택한 책 정보)
     */
    @Transactional
    public BookSelectResponse selectBook(Long bookId, Long memberId) {
        // 1. 책 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("책을 찾을 수 없습니다: " + bookId));

        // 2. 멤버 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("모임원을 찾을 수 없습니다: " + memberId));

        // 3. 같은 모임의 다른 활성 멤버가 이미 이 책을 선택했는지 확인
        List<Member> groupMembers = memberRepository.findByGroup_GroupIdAndStatus(
                member.getGroup().getGroupId(), MemberStatus.ACTIVE);
        boolean isAlreadySelected = groupMembers.stream()
                .filter(m -> !m.getMemberId().equals(memberId)) // 자기 자신 제외
                .anyMatch(m -> bookId.equals(m.getSelectedBookId()));

        if (isAlreadySelected) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_SELECTED);
        }

        // 4. 멤버의 선택 책 업데이트
        member.selectBook(bookId);

        // 5. 책 인기도 증가 (교환독서 선택 x 5)
        for (int i = 0; i < 5; i++) {
            book.increasePopularity();
        }

        // 6. 모든 모임원의 정보와 각자 선택한 책 정보 조회
        List<MemberBookInfo> memberBookInfos = groupMembers.stream()
                .map(m -> {
                    Book selectedBook = null;
                    if (m.getSelectedBookId() != null) {
                        selectedBook = bookRepository.findById(m.getSelectedBookId()).orElse(null);
                    }
                    return new MemberBookInfo(m, selectedBook);
                })
                .collect(Collectors.toList());

        return new BookSelectResponse(memberId, bookId, book.getTitle(), memberBookInfos);
    }
}
