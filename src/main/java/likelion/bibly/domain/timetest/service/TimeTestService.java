package likelion.bibly.domain.timetest.service;

/**
 * 시간 테스트 서비스 인터페이스
 */
public interface TimeTestService {

	/**
	 * 시간을 X일 앞으로 점프 (모든 배정을 X일 과거로 이동)
	 *
	 * @param groupId 모임 ID
	 * @param days 점프할 일수
	 * @return 결과 메시지
	 */
	String jumpForward(Long groupId, int days);

	/**
	 * 다음 회차로 점프 (readingPeriod일 점프)
	 *
	 * @param groupId 모임 ID
	 * @return 결과 메시지
	 */
	String jumpToNextCycle(Long groupId);

	/**
	 * 재시작 가능 상태로 점프 (모든 회차 완료)
	 *
	 * @param groupId 모임 ID
	 * @return 결과 메시지
	 */
	String jumpToRestart(Long groupId);
}
