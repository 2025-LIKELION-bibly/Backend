package likelion.bibly.domain.session.service;


import likelion.bibly.domain.session.dto.ReadingSessionResponse;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingSessionService {

    private final ReadingSessionRepository readingSessionRepository;

    /** 책읽기 화면: 현재 진행 중인 독서 세션 조회 */

    // 반환 타입을 DTO 리스트로 변경
    public List<ReadingSessionResponse> getOngoingSessionsForMember(Long memberId) {

        // DB에서 엔티티 리스트 조회
        // TODO: "진행 중인" 세션만 가져오는 로직으로 수정 필요 (예: mode, status 등)
        List<ReadingSession> sessionEntities = readingSessionRepository.findByMember_MemberId(memberId);

        List<ReadingSessionResponse> sessionDtos = sessionEntities.stream()
                .map(session -> new ReadingSessionResponse(session)) // DTO 생성자 호출
                .collect(Collectors.toList());

        // DTO 리스트 반환
        return sessionDtos;
    }

    // TODO: ReadingSession 관련 쓰기 로직 추가
}