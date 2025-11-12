package likelion.bibly.domain.home.dto;

import lombok.Data;
import java.util.List;

/**
 * 홈(Home) 탭의 임시 응답 DTO
 */
@Data
public class HomeResponse {

    // 1. 현재 모임 정보
    private Object currentGroup;

    // 2. 모임 구성원 목록
    private List<?> members;

    // 3. 현재 읽는 중인 책 정보
    private Object currentlyReadingBook;

    // 4. 북마크 목록
    private List<?> bookmarks;

    // 5. 코멘트 목록
    private List<?> comments;

    // 6. (Optional) 모임 전환(Switcher)을 위한 목록
    private List<?> allMyGroups;
}