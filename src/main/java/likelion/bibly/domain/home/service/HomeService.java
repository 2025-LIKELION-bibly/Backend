package likelion.bibly.domain.home.service;

import likelion.bibly.domain.home.dto.HomeResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * NavigatorController의 의존성 주입용 임시 파일
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // CUD가 없으므로 우선 readOnly로 설정
public class HomeService {

    // TODO: 추후 필요한 Repository들을 주입
    // private final MemberRepository memberRepository;
    // private final GroupRepository groupRepository;
    // private final ReadingSessionRepository readingSessionRepository;

    /**
     * 홈 탭에 필요한 데이터를 조회하여 HomeResponse로 반환
     * (현재는 임시 DTO 객체를 생성하여 반환)
     */
    public HomeResponse getHomeData(Long memberId) {
        
        HomeResponse response = new HomeResponse();

        // TODO: 추후 비즈니스 로직 구현
        
        //    - memberId로 사용자 정보 조회
        //    - 사용자의 현재 활성화된 모임(currentGroup) 조회
        //    - 모임의 구성원(members) 조회
        //    - 사용자의 현재 읽는 책(currentlyReadingBook) 조회
        //    - 관련 북마크(bookmarks), 코멘트(comments) 조회

        // 임시 dto 반환
        return response;
    }
}