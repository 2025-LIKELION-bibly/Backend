package likelion.bibly.domain.home.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.bookmark.dto.BookmarkListResponse;
import likelion.bibly.domain.bookshelf.dto.TraceGroupResponse;
import likelion.bibly.domain.bookshelf.dto.TraceItemResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeResponse {

    // --- 1. 그룹/멤버 정보 (네비게이터 영역) ---

    // 로그인된 사용자가 현재 선택한 모임 정보
    private Long currentGroupId;
    private String currentGroupName;

    // 해당 모임에 참여 중인 멤버들의 닉네임 목록
    private List<String> groupMemberNicknames;

    // --- 2. 독서 상태 정보 (ReadingAssignment 기반) ---

    // 현재 읽고 있는 책 및 다음에 읽을 책 (기존 CurrentReadingBookResponse 재사용)
    private CurrentReadingBookResponse currentReadingBookInfo;

    // --- 3. 컨텐츠 목록 정보 ---

    // A. 책장 흔적 모아보기 (TraceGroupResponse)
    @Schema(description = "흔적 모아보기 (BookShelf Group 단위)")
    private List<TraceGroupResponse> traceGroups;

    // B. 책장 흔적 전체보기 (TraceItemResponse) - 최신순 N개 (예시)
    @Schema(description = "최신 흔적 전체보기 요약 (BookShelf Item 단위)")
    private List<TraceItemResponse> recentTraceItems;

    // C. 현재 세션의 북마크 목록 (Bookmark API 결과)
    @Schema(description = "현재 독서 세션의 북마크 목록")
    private List<BookmarkListResponse> recentBookmarks;
}