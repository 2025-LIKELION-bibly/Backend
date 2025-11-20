package likelion.bibly.domain.book.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        return new BookDetailResponse(book);
    }

    /**
     * D.2.2 책 선택 (교환 책으로 선택)
     * @param bookId 선택할 책 ID
     * @param userId 사용자 ID
     * @param groupId 모임 ID
     * @return 선택 완료 응답 (모든 모임원 + 각자 선택한 책 정보)
     */
    @Transactional
    public BookSelectResponse selectBook(Long bookId, String userId, Long groupId) {
        // 책 조회, 멤버 조회
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 탈퇴한 멤버는 책을 선택할 수 없음
        if (member.getStatus() != MemberStatus.ACTIVE) {
            throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
        }

        // 같은 모임의 다른 멤버가 이미 이 책을 선택했는지 확인
        List<Member> groupMembers = memberRepository.findByGroup_GroupIdAndStatus(
                groupId, MemberStatus.ACTIVE);
        boolean isAlreadySelected = groupMembers.stream()
                .filter(m -> !m.getMemberId().equals(member.getMemberId())) // 자기 자신 제외
                .anyMatch(m -> bookId.equals(m.getSelectedBookId()));

        if (isAlreadySelected) {
            throw new BusinessException(ErrorCode.BOOK_ALREADY_SELECTED);
        }

        member.selectBook(bookId);

        // 책 인기도 5점 증가 (교환독서 선택)
        for (int i = 0; i < 5; i++) {
            book.increasePopularity(); // +1씩 5회
        }

        List<MemberBookInfo> memberBookInfos = groupMembers.stream()
                .map(m -> {
                    Book selectedBook = null;
                    if (m.getSelectedBookId() != null) {
                        selectedBook = bookRepository.findById(m.getSelectedBookId()).orElse(null);
                    }
                    return new MemberBookInfo(m, selectedBook, null);
                })
                .collect(Collectors.toList());

        return new BookSelectResponse(member.getMemberId(), bookId, book.getTitle(), memberBookInfos);
    }
}