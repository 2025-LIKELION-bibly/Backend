package likelion.bibly.domain.assignment.service;

import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.assignment.dto.response.NextReadingBookResponse;

/**
 * 배정 관리 서비스 인터페이스 (교환독서 배정, 한줄평, 다음 책 안내)
 */
public interface AssignmentService {

	/**
	 * G.1.1 한줄평 등록
	 *
	 * @param assignmentId 배정 ID
	 * @param userId 사용자 ID
	 * @param review 한줄평 (최대 40자)
	 * @return 업데이트된 배정 정보
	 * @throws likelion.bibly.global.exception.BusinessException A001, A002
	 */
	AssignmentResponse writeReview(Long assignmentId, String userId, String review);

	/**
	 * G.1.2 현재 배정 조회
	 * 현재 회차에서 로그인한 사용자가 배정받은 책 정보를 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 현재 배정 정보 (모든 회차 완료 시 A001 에러)
	 * @throws likelion.bibly.global.exception.BusinessException G001, M001, A001
	 */
	AssignmentResponse getCurrentAssignment(String userId, Long groupId);

	/**
	 * G.1.4 재시작 안내화면
	 * 현재 회차의 모든 모임원의 배정 정보를 조회합니다.
	 * 선택된 책은 초기화하지 않으며, 탈퇴한 모임원도 삭제하지 않습니다.
	 *
	 * @param groupId 모임 ID
	 * @return 현재 회차의 모임원별 배정 정보
	 * @throws likelion.bibly.global.exception.BusinessException G001
	 */
	CurrentAssignmentResponse getCurrentAssignments(Long groupId);

	/**
	 * 교환독서 시작 시 모든 회차 배정 생성
	 * GroupService의 startGroup에서 호출됩니다.
	 * 모임원 수만큼의 회차를 미리 생성하여 전체 독서 스케줄을 확정합니다.
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 */
	void createInitialAssignments(Long groupId, Integer readingPeriod);

	/**
	 * 재시작 시 추가 회차 배정 생성
	 * 기존 배정을 보존하고 새로운 라운드의 회차를 추가로 생성합니다.
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 */
	void createAdditionalAssignments(Long groupId, Integer readingPeriod);

	/**
	 * 현재 읽고 있는 책 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 현재 읽고 있는 책 정보 (표지, 남은 기간, 교환일, 앞으로 읽을 책들)
	 * @throws likelion.bibly.global.exception.BusinessException G001, M001, A001
	 */
	CurrentReadingBookResponse getCurrentReadingBook(String userId, Long groupId);

	/**
	 * 다음에 읽을 책 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 다음에 읽을 책 정보 (표지, 읽을 수 있는 날짜, 현재 독자, 책 정보, 한줄평들)
	 * @throws likelion.bibly.global.exception.BusinessException G001, M001, A001
	 */
	NextReadingBookResponse getNextReadingBook(String userId, Long groupId);
}
