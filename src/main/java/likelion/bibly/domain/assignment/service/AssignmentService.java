package likelion.bibly.domain.assignment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.RotateBooksResponse;
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

	private static final Random RANDOM = new Random();

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
		ReadingAssignment assignment = assignmentRepository.findById(assignmentId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		// 권한 확인: 해당 배정의 멤버가 맞는지 확인
		if (!assignment.getMember().getUserId().equals(userId)) {
			throw new BusinessException(ErrorCode.ACCESS_DENIED);
		}

		assignment.writeReview(review);

		return AssignmentResponse.from(assignment);
	}

	/**
	 * G.1.2 현재 배정 조회
	 * 현재 회차에서 로그인한 사용자가 배정받은 책 정보를 반환합니다.
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 현재 배정 정보 (모든 회차 완료 시 A001 에러)
	 * @throws BusinessException G001, M001, A001
	 */
	public AssignmentResponse getCurrentAssignment(String userId, Long groupId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		List<ReadingAssignment> assignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentMaxCycle = assignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		if (currentMaxCycle > 0 && currentMaxCycle % memberCount == 0) {
			ReadingAssignment lastAssignment = assignments.stream()
				.filter(a -> a.getCycleNumber().equals(currentMaxCycle))
				.max((a1, a2) -> a1.getEndDate().compareTo(a2.getEndDate()))
				.orElse(null);

			if (lastAssignment != null && LocalDateTime.now().isAfter(lastAssignment.getEndDate())) {
				throw new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND);
			}
		}

		ReadingAssignment currentAssignment = assignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
			.filter(a -> a.getCycleNumber().equals(currentMaxCycle))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		return AssignmentResponse.from(currentAssignment);
	}

	/**
	 * G.1.4 재시작 안내화면
	 * 현재 회차의 모든 모임원의 배정 정보를 조회합니다.
	 * 선택된 책은 초기화하지 않으며, 탈퇴한 모임원도 삭제하지 않습니다.
	 *
	 * @param groupId 모임 ID
	 * @return 현재 회차의 모임원별 배정 정보
	 * @throws BusinessException G001
	 */
	@Transactional(readOnly = true)
	public CurrentAssignmentResponse getCurrentAssignments(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		List<ReadingAssignment> currentAssignments = allAssignments.stream()
			.filter(a -> a.getCycleNumber().equals(currentCycle))
			.toList();

		List<CurrentAssignmentResponse.MemberAssignmentInfo> memberAssignments = activeMembers.stream()
			.map(member -> {
				CurrentAssignmentResponse.MemberAssignmentInfo.MemberAssignmentInfoBuilder builder =
					CurrentAssignmentResponse.MemberAssignmentInfo.builder()
						.memberId(member.getMemberId())
						.nickname(member.getNickname())
						.color(member.getColor());

				// selected_book_id 기반으로 선택한 책 정보 반환
				if (member.getSelectedBookId() != null) {
					Book selectedBook = bookRepository.findById(member.getSelectedBookId()).orElse(null);
					if (selectedBook != null) {
						builder.hasBook(true)
							.bookId(selectedBook.getBookId())
							.bookTitle(selectedBook.getTitle())
							.coverImageUrl(selectedBook.getCoverUrl());
					} else {
						builder.hasBook(false);
					}
				} else {
					builder.hasBook(false);
				}

				return builder.build();
			})
			.toList();

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
	 * @param userId 로그인한 사용자 ID
	 * @return 로그인 사용자의 배정 정보 + 다른 모임원들의 배정 정보
	 * @throws BusinessException G001, M001
	 */
	@Transactional
	public RotateBooksResponse rotateBooks(Long groupId, String userId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member requester = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);
		Integer currentMaxCycle = allAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		Integer nextCycle = currentMaxCycle + 1;

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);
		int memberCount = activeMembers.size();

		List<Book> allBooks = bookRepository.findAll();

		for (Member member : activeMembers) {
			if (member.getSelectedBookId() == null) {
				Book randomBook = allBooks.get(RANDOM.nextInt(allBooks.size()));
				member.selectBook(randomBook.getBookId());
			}
		}

		createCycleAssignments(groupId, group.getReadingPeriod(), nextCycle);

		List<ReadingAssignment> newAssignments = assignmentRepository.findByGroup_GroupId(groupId).stream()
			.filter(a -> a.getCycleNumber().equals(nextCycle))
			.toList();

		// 로그인한 사용자의 배정 정보
		ReadingAssignment myAssignment = newAssignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(requester.getMemberId()))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		AssignmentResponse myAssignmentResponse = AssignmentResponse.from(myAssignment);

		// 다른 모임원들의 배정 정보 (memberId + bookId)
		java.util.List<RotateBooksResponse.OtherMemberAssignment> otherMembers = newAssignments.stream()
			.filter(a -> !a.getMember().getMemberId().equals(requester.getMemberId()))
			.map(a -> RotateBooksResponse.OtherMemberAssignment.builder()
				.memberId(a.getMember().getMemberId())
				.bookId(a.getBook().getBookId())
				.build())
			.toList();

		return RotateBooksResponse.builder()
			.myAssignment(myAssignmentResponse)
			.otherMembers(otherMembers)
			.build();
	}

	/**
	 * 회차별 배정 생성 (공통 로직)
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 * @param cycleNumber 회차 번호
	 */
	private void createCycleAssignments(Long groupId, Integer readingPeriod, Integer cycleNumber) {
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 회차 시작/종료일 설정
		LocalDateTime startDate = LocalDateTime.now();
		LocalDateTime endDate = startDate.plusDays(readingPeriod);

		// 각 멤버에게 다른 멤버가 선택한 책 배정
		// cycleNumber에 따라 순환 위치 조정
		for (int i = 0; i < members.size(); i++) {
			Member member = members.get(i);
			int bookOwnerIndex = (i + cycleNumber) % members.size();
			Member bookOwner = members.get(bookOwnerIndex);

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