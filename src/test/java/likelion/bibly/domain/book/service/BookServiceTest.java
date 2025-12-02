package likelion.bibly.domain.book.service;

import likelion.bibly.domain.book.dto.response.BookDetailResponse;
import likelion.bibly.domain.book.dto.response.BookSelectResponse;
import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberRole;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.page.entity.PageContent;
import likelion.bibly.domain.page.repository.PageRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import likelion.bibly.global.util.PaginationUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * BookService 단위 테스트
 * Service 계층의 비즈니스 로직을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PageRepository pageRepository;

    @Mock
    private PaginationUtil paginationUtil;

    @Test
    @DisplayName("새로 나온 책 목록 조회 성공 테스트")
    void getNewBooksSuccessTest() {
        // Given
        List<Book> books = List.of(
                Book.builder().title("책1").author("저자1").build(),
                Book.builder().title("책2").author("저자2").build(),
                Book.builder().title("책3").author("저자3").build()
        );

        given(bookRepository.findTop20ByOrderByCreatedAtDesc()).willReturn(books);

        // When
        List<BookSimpleResponse> response = bookService.getNewBooks();

        // Then
        assertThat(response).hasSize(3);
        assertThat(response.get(0).getTitle()).isEqualTo("책1");
        verify(bookRepository).findTop20ByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("인기 있는 책 목록 조회 성공 테스트")
    void getPopularBooksSuccessTest() {
        // Given
        Book popularBook1 = Book.builder().title("인기책1").author("저자1").build();
        popularBook1.increasePopularity();
        popularBook1.increasePopularity();

        Book popularBook2 = Book.builder().title("인기책2").author("저자2").build();
        popularBook2.increasePopularity();

        List<Book> books = List.of(popularBook1, popularBook2);

        given(bookRepository.findTop20ByOrderByPopularityScoreDesc()).willReturn(books);

        // When
        List<BookSimpleResponse> response = bookService.getPopularBooks();

        // Then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getTitle()).isEqualTo("인기책1");
        verify(bookRepository).findTop20ByOrderByPopularityScoreDesc();
    }

    @Test
    @DisplayName("책 상세 정보 조회 성공 테스트")
    void getBookDetailSuccessTest() {
        // Given
        Long bookId = 1L;
        Book book = Book.builder()
                .title("상세 테스트 책")
                .author("상세 테스트 저자")
                .genre("Fiction")
                .pageCount(300)
                .description("상세 설명")
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));

        // When
        BookDetailResponse response = bookService.getBookDetail(bookId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("상세 테스트 책");
        assertThat(response.getAuthor()).isEqualTo("상세 테스트 저자");
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("책 상세 정보 조회 실패 - 존재하지 않는 책")
    void getBookDetailFailNotFoundTest() {
        // Given
        Long invalidBookId = 999L;
        given(bookRepository.findById(invalidBookId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> bookService.getBookDetail(invalidBookId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_NOT_FOUND);
    }

    @Test
    @DisplayName("책 선택 성공 테스트")
    void selectBookSuccessTest() {
        // Given
        Long bookId = 1L;
        String userId = "test-user";
        Long groupId = 1L;

        Book book = Book.builder()
                .title("선택 테스트 책")
                .author("선택 테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(member));

        // When
        BookSelectResponse response = bookService.selectBook(bookId, userId, groupId);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getSelectedBookTitle()).isEqualTo("선택 테스트 책");
        assertThat(member.getSelectedBookId()).isEqualTo(bookId);
        assertThat(book.getPopularityScore()).isEqualTo(5); // 5점 증가
        verify(bookRepository).findById(bookId);
    }

    @Test
    @DisplayName("책 선택 실패 - 이미 선택된 책")
    void selectBookFailAlreadySelectedTest() {
        // Given
        Long bookId = 1L;
        String userId = "new-user";
        Long groupId = 1L;

        Book book = Book.builder()
                .title("중복 선택 테스트 책")
                .author("테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member newMember = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("신규멤버")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        Member existingMember = Member.builder()
                .group(group)
                .userId("existing-user")
                .nickname("기존멤버")
                .color("BLUE")
                .role(MemberRole.MEMBER)
                .build();
        existingMember.selectBook(bookId); // 이미 같은 책 선택됨

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(newMember));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(newMember, existingMember));

        // When & Then
        assertThatThrownBy(() -> bookService.selectBook(bookId, userId, groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.BOOK_ALREADY_SELECTED);
    }

    @Test
    @DisplayName("책 선택 실패 - 탈퇴한 멤버")
    void selectBookFailWithdrawnMemberTest() {
        // Given
        Long bookId = 1L;
        String userId = "withdrawn-user";
        Long groupId = 1L;

        Book book = Book.builder()
                .title("탈퇴 테스트 책")
                .author("테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member withdrawnMember = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("탈퇴멤버")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();
        withdrawnMember.withdraw(); // 탈퇴됨

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(withdrawnMember));

        // When & Then
        assertThatThrownBy(() -> bookService.selectBook(bookId, userId, groupId))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MEMBER_NOT_FOUND);
    }

    @Test
    @DisplayName("페이지 내용 저장 성공 테스트")
    void saveBookContentAsPagesSuccessTest() throws Exception {
        // Given
        Long bookId = 1L;
        String content = "테스트 책 내용입니다. 이것은 페이지로 분할될 내용입니다.";
        Book book = Book.builder()
                .title("페이지 테스트 책")
                .author("테스트 저자")
                .content(content)
                .build();

        List<String> pages = List.of("페이지1 내용", "페이지2 내용", "페이지3 내용");

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(paginationUtil.splitTextByDualLimit(content)).willReturn(pages);

        // When
        bookService.saveBookContentAsPages(bookId);

        // Then
        assertThat(book.getPageCount()).isEqualTo(3);
        verify(bookRepository).findById(bookId);
        verify(paginationUtil).splitTextByDualLimit(content);
        verify(pageRepository).deleteByBook(book);
        verify(pageRepository, times(3)).save(any(PageContent.class));
    }

    @Test
    @DisplayName("페이지 내용 조회 성공 테스트")
    void getPageContentSuccessTest() {
        // Given
        Long bookId = 1L;
        Integer pageNumber = 1;
        Book book = Book.builder()
                .title("페이지 조회 테스트 책")
                .author("테스트 저자")
                .build();

        PageContent pageContent = PageContent.builder()
                .pageNumber(pageNumber)
                .content("페이지 1 내용입니다.")
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(pageRepository.findByBookAndPageNumber(book, pageNumber))
                .willReturn(Optional.of(pageContent));

        // When
        String content = bookService.getPageContent(bookId, pageNumber);

        // Then
        assertThat(content).isEqualTo("페이지 1 내용입니다.");
        verify(bookRepository).findById(bookId);
        verify(pageRepository).findByBookAndPageNumber(book, pageNumber);
    }

    @Test
    @DisplayName("인기도 증가 검증 테스트")
    void popularityIncreaseVerificationTest() {
        // Given
        Long bookId = 1L;
        String userId = "test-user";
        Long groupId = 1L;

        Book book = Book.builder()
                .title("인기도 테스트 책")
                .author("테스트 저자")
                .build();

        Group group = Group.builder()
                .groupName("테스트 모임")
                .readingPeriod(14)
                .inviteCode("1234")
                .build();

        Member member = Member.builder()
                .group(group)
                .userId(userId)
                .nickname("테스터")
                .color("RED")
                .role(MemberRole.MEMBER)
                .build();

        given(bookRepository.findById(bookId)).willReturn(Optional.of(book));
        given(memberRepository.findByGroup_GroupIdAndUserId(groupId, userId))
                .willReturn(Optional.of(member));
        given(memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE))
                .willReturn(List.of(member));

        // When
        int initialPopularity = book.getPopularityScore();
        bookService.selectBook(bookId, userId, groupId);
        int finalPopularity = book.getPopularityScore();

        // Then
        assertThat(finalPopularity).isEqualTo(initialPopularity + 5);
    }
}
