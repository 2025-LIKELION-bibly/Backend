package likelion.bibly.domain.group.dto.response;

import java.util.List;

import likelion.bibly.domain.book.dto.response.MemberBookInfo;
import lombok.Getter;

/**
 * 모임의 모든 모임원과 각자 선택한 책 정보 응답 DTO
 */
@Getter
public class GroupMembersBookResponse {
    private Long groupId;
    private String groupName;
    private List<MemberBookInfo> members;

    public GroupMembersBookResponse(Long groupId, String groupName, List<MemberBookInfo> members) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.members = members;
    }
}
