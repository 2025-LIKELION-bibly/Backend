package likelion.bibly.domain.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;

/**
 * 배정 관리 서비스 (교환독서 배정, 한줄평, 다음 책 안내)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentService {

	private final ReadingAssignmentRepository assignmentRepository;
	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;

	/**
	 * G.1.1 한줄평 등록
	 *
	 * @param assignmentId 배정 ID
	 * @param userId 사용자 ID
	 * @param review 한줄평 (최대 40자)
	 * @return 업데이트된 배정 정보
	 * @throws BusinessException A001, A002
	 */
	@Transactional
	public AssignmentResponse writeReview(Long assignmentId, String userId, String review) {
		// 배정 조회
		ReadingAssignment assignment = assignmentRepository.findById(assignmentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		// 권한 확인: 해당 배정의 멤버가 맞는지 확인
		if (!assignment.getMember().getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		// 한줄평 등록 (@Valid 어노테이션이 길이 검증 수행)
		assignment.writeReview(review);

		return AssignmentResponse.from(assignment);
	}

	/**
	 * G.1.2 다음 책 안내
	 * 교환일이 되었을 때 다음에 읽을 책 정보를 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 다음 배정 정보
	 * @throws BusinessException G001, M001, A001
	 */
	public AssignmentResponse getNextAssignment(String userId, Long groupId) {
		// 모임 검증
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 멤버 검증
		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		// 현재 회차의 다음 배정 조회 (가장 최근 배정)
		List<ReadingAssignment> assignments = assignmentRepository.findByGroup_GroupId(groupId);

		// 해당 멤버의 배정 중 가장 최근 것 찾기
		ReadingAssignment latestAssignment = assignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
			.max((a1, a2) -> a1.getCycleNumber().compareTo(a2.getCycleNumber()))
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		return AssignmentResponse.from(latestAssignment);
	}

	/**
	 * G.2.1 재시작 안내화면
	 * 현재 회차의 모든 모임원의 배정 정보를 조회합니다.
	 *
	 * @param groupId 모임 ID
	 * @return 현재 회차의 모임원별 배정 정보
	 * @throws BusinessException G001
	 */
	public CurrentAssignmentResponse getCurrentAssignments(Long groupId) {
		// 모임 검증
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 활성 모임원 조회
		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		// 모든 배정 조회
		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		// 현재 회차 계산 (가장 최근 회차)
		Integer currentCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		// 현재 회차의 배정만 필터링
		List<ReadingAssignment> currentAssignments = allAssignments.stream()
			.filter(a -> a.getCycleNumber().equals(currentCycle))
			.collect(Collectors.toList());

		// 모임원별 배정 정보 매핑
		List<CurrentAssignmentResponse.MemberAssignmentInfo> memberAssignments = activeMembers.stream()
			.map(member -> {
				// 해당 멤버의 현재 회차 배정 찾기
				ReadingAssignment assignment = currentAssignments.stream()
					.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
					.findFirst()
					.orElse(null);

				CurrentAssignmentResponse.MemberAssignmentInfo.MemberAssignmentInfoBuilder builder =
					CurrentAssignmentResponse.MemberAssignmentInfo.builder()
						.memberId(member.getMemberId())
						.nickname(member.getNickname())
						.color(member.getColor())
						.hasBook(assignment != null);

				if (assignment != null) {
					Book book = assignment.getBook();
					builder.bookId(book.getBookId())
						.bookTitle(book.getTitle())
						.coverImageUrl(book.getCoverUrl());
				}

				return builder.build();
			})
			.collect(Collectors.toList());

		return CurrentAssignmentResponse.builder()
			.groupId(group.getGroupId())
			.groupName(group.getGroupName())
			.currentCycle(currentCycle)
			.memberAssignments(memberAssignments)
			.build();
	}

	/**
	 * 교환독서 시작 시 첫 회차 배정 생성
	 * GroupService의 startGroup에서 호출됩니다.
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 */
	@Transactional
	public void createInitialAssignments(Long groupId, Integer readingPeriod) {
		createCycleAssignments(groupId, readingPeriod, 1);
	}

	/**
	 * 책 교환 (다음 회차 배정 생성)
	 *
	 * @param groupId 모임 ID
	 * @return 업데이트된 배정 정보 리스트
	 * @throws BusinessException G001
	 */
	@Transactional
	public List<AssignmentResponse> rotateBooks(Long groupId) {
		// 모임 조회
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 현재 최대 회차 확인
		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);
		Integer currentMaxCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		// 다음 회차 번호
		Integer nextCycle = currentMaxCycle + 1;

		// 다음 회차 배정 생성
		createCycleAssignments(groupId, group.getReadingPeriod(), nextCycle);

		// 새로 생성된 배정 조회 및 반환
		List<ReadingAssignment> newAssignments = assignmentRepository.findByGroup_GroupId(groupId).stream()
			.filter(a -> a.getCycleNumber().equals(nextCycle))
			.collect(Collectors.toList());

		return newAssignments.stream()
			.map(AssignmentResponse::from)
			.collect(Collectors.toList());
	}

	/**
	 * 회차별 배정 생성 (공통 로직)
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 * @param cycleNumber 회차 번호
	 */
	private void createCycleAssignments(Long groupId, Integer readingPeriod, Integer cycleNumber) {
		// 모임의 모든 활성 멤버 조회
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		// 모임 조회
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 회차 시작/종료일 설정
		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = startDate.plusDays(readingPeriod);

		// 각 멤버에게 다른 멤버가 선택한 책 배정
		// cycleNumber에 따라 순환 위치 조정
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			// cycleNumber만큼 회전하여 배정
			int bookOwnerIndex = (i + cycleNumber) % members.size();
			Member bookOwner = members.get(bookOwnerIndex);

			// 책 조회
			Book book = bookRepository.findById(bookOwner.getSelectedBookId())
				.orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

			// 배정 생성
			ReadingAssignment assignment = ReadingAssignment.builder()
				.book(book)
				.group(group)
				.member(member)
				.cycleNumber(cycleNumber)
				.startDate(startDate)
				.endDate(endDate)
				.build();

			assignmentRepository.save(assignment);
		}
	}
}
