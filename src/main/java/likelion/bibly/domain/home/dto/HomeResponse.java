package likelion.bibly.domain.home.dto;

import likelion.bibly.domain.book.dto.response.BookSimpleResponse;
import likelion.bibly.domain.group.dto.response.UserGroupsInfoResponse;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class HomeResponse {

    // E.1.1 ~ E.1.3 모임 정보 섹션
    private Long currentGroupId;                // 현재 선택된 그룹 ID
    private List<UserGroupsInfoResponse> groupList;  // 참여한 모든 그룹 목록 (순서대로)
    private List<String> memberNicknames;      // 현재 그룹 구성원의 닉네임 첫 글자

    // E.1.4 현재 읽고 있는 책 섹션
    private Long currentSessionId;             // 현재 진행 중인 Active Session ID (2차 호출에 사용)
    private Long currentBookId;                // 현재 읽는 책 ID (2차 호출에 사용)
    private BookSimpleResponse currentBookInfo; // 현재 읽는 책의 간략 정보
    private String exchangeDday;               // 교환 독서일까지 남은 기간 (D-day)
    private LocalDate nextExchangeDate;        // 다음 교환 독서일

    // E.1.5 다음에 읽을 책 섹션
    private BookSimpleResponse nextBookInfo;    // 다음에 읽을 책의 간략 정보
    private String nextReadStartDate;          // 다음 책 읽기 시작일 (예: "~월~일부터 읽을 수 있어요")
    private List<MemberInfoResponse> readers;  // 이미 읽은 모임원 목록
    private List<String> latestReviews;        // 이미 읽은 사람들의 최신 한줄평 (예시)

    // E.1.5의 구성원 정보 DTO (내부 클래스로 가정)
    @Data
    @Builder
    public static class MemberInfoResponse {
        private String nickname;
        private String iconUrl;
    }
}