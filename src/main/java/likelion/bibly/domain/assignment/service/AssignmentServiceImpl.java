package likelion.bibly.domain.assignment.service;

import likelion.bibly.domain.assignment.dto.response.AssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentAssignmentResponse;
import likelion.bibly.domain.assignment.dto.response.CurrentReadingBookResponse;
import likelion.bibly.domain.assignment.dto.response.NextReadingBookResponse;
import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.book.entity.Book;
import likelion.bibly.domain.book.repository.BookRepository;
import likelion.bibly.domain.group.entity.Group;
import likelion.bibly.domain.group.repository.GroupRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.entity.Progress;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 배정 관리 서비스 구현체 (교환독서 배정, 한줄평, 다음 책 안내)
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AssignmentServiceImpl implements AssignmentService {

	private final ReadingAssignmentRepository assignmentRepository;
	private final GroupRepository groupRepository;
	private final MemberRepository memberRepository;
	private final BookRepository bookRepository;
    private final ProgressRepository progressRepository;

	/**
	 * 시간 기준으로 현재 진행 중인 회차 계산
	 * 현재 시간이 속한 회차의 번호를 반환합니다.
	 * 모든 회차 시작 전이면 첫 회차, 모든 회차 종료 후면 마지막 회차를 반환합니다.
	 *
	 * @param assignments 모든 배정 목록
	 * @return 현재 회차 번호
	 */
	private Integer getCurrentCycle(List<ReadingAssignment> assignments) {
		LocalDateTime now = LocalDateTime.now();

		return assignments.stream()
			.filter(a -> !now.isBefore(a.getStartDate()) && !now.isAfter(a.getEndDate()))
			.map(ReadingAssignment::getCycleNumber)
			.findFirst()
			.orElseGet(() -> {
				// 현재 진행 중인 회차가 없을 때
				LocalDateTime firstStartDate = assignments.stream()
					.map(ReadingAssignment::getStartDate)
					.min(LocalDateTime::compareTo)
					.orElse(LocalDateTime.now());

				if (now.isBefore(firstStartDate)) {
					// 모든 회차 시작 전 → 첫 회차 반환
					return assignments.stream()
						.map(ReadingAssignment::getCycleNumber)
						.min(Integer::compareTo)
						.orElse(1);
				} else {
					// 모든 회차 종료 후 → 마지막 회차 반환
					return assignments.stream()
						.map(ReadingAssignment::getCycleNumber)
						.max(Integer::compareTo)
						.orElse(1);
				}
			});
	}

	/**
	 * G.1.1 한줄평 등록
	 *
	 * @param assignmentId 배정 ID
	 * @param userId 사용자 ID
	 * @param review 한줄평 (최대 40자)
	 * @return 업데이트된 배정 정보
	 * @throws BusinessException A001, A002
	 */
	@Override
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
	@Override
	public AssignmentResponse getCurrentAssignment(String userId, Long groupId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		List<ReadingAssignment> assignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentCycle = getCurrentCycle(assignments);

		ReadingAssignment currentAssignment = assignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
			.filter(a -> a.getCycleNumber().equals(currentCycle))
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
	@Override
	@Transactional(readOnly = true)
	public CurrentAssignmentResponse getCurrentAssignments(Long groupId) {
		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE);

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentCycle = getCurrentCycle(allAssignments);

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
	 * 교환독서 시작 시 모든 회차 배정 생성
	 * GroupService의 startGroup에서 호출됩니다.
	 * 모임원 수만큼의 회차를 미리 생성하여 전체 독서 스케줄을 확정합니다.
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 */
	@Override
	@Transactional
	public void createInitialAssignments(Long groupId, Integer readingPeriod) {
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE).stream()
			.sorted((m1, m2) -> m1.getMemberId().compareTo(m2.getMemberId()))
			.collect(Collectors.toList());
		int memberCount = members.size();

		// 각 회차의 시작일을 계산하며 모든 회차 생성
		LocalDateTime currentStartDate = LocalDateTime.now();
		for (int cycle = 1; cycle <= memberCount; cycle++) {
			createCycleAssignments(groupId, readingPeriod, cycle, currentStartDate);
			currentStartDate = currentStartDate.plusDays(readingPeriod);
		}
	}

	/**
	 * 재시작 시 추가 회차 배정 생성
	 * 기존 배정을 보존하고 새로운 라운드의 회차를 추가로 생성합니다.
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 */
	@Override
	@Transactional
	public void createAdditionalAssignments(Long groupId, Integer readingPeriod) {
		List<ReadingAssignment> existingAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		// 기존 최대 회차 번호 찾기
		Integer maxCycleNumber = existingAssignments.stream()
			.map(ReadingAssignment::getCycleNumber)
			.max(Integer::compareTo)
			.orElse(0);

		// 마지막 회차의 종료일 찾기 (다음 회차 시작일 계산용)
		LocalDateTime nextStartDate = existingAssignments.stream()
			.filter(a -> a.getCycleNumber().equals(maxCycleNumber))
			.map(ReadingAssignment::getEndDate)
			.findFirst()
			.orElse(LocalDateTime.now());

		// 현재 활성 모임원 수만큼 새로운 회차 생성
		List<Member> activeMembers = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE).stream()
			.sorted((m1, m2) -> m1.getMemberId().compareTo(m2.getMemberId()))
			.collect(Collectors.toList());
		int memberCount = activeMembers.size();

		// 다음 회차부터 생성 (예: 기존 1~4회차 → 5~8회차 추가)
		LocalDateTime currentStartDate = nextStartDate;
		for (int i = 1; i <= memberCount; i++) {
			int cycleNumber = maxCycleNumber + i;
			createCycleAssignments(groupId, readingPeriod, cycleNumber, currentStartDate);
			currentStartDate = currentStartDate.plusDays(readingPeriod);
		}
	}

	/**
	 * 회차별 배정 생성 (공통 로직)
	 *
	 * @param groupId 모임 ID
	 * @param readingPeriod 독서 기간 (일)
	 * @param cycleNumber 회차 번호
	 * @param startDate 회차 시작일
	 */
	private void createCycleAssignments(Long groupId, Integer readingPeriod, Integer cycleNumber, LocalDateTime startDate) {
		List<Member> members = memberRepository.findByGroup_GroupIdAndStatus(groupId, MemberStatus.ACTIVE).stream()
			.sorted((m1, m2) -> m1.getMemberId().compareTo(m2.getMemberId()))
			.collect(Collectors.toList());

		Group group = groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		// 회차 종료일 설정
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

	/**
	 * 현재 읽고 있는 책 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 현재 읽고 있는 책 정보 (표지, 남은 기간, 교환일, 앞으로 읽을 책들)
	 * @throws BusinessException G001, M001, A001
	 */
	@Override
	public CurrentReadingBookResponse getCurrentReadingBook(String userId, Long groupId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentCycle = getCurrentCycle(allAssignments);

		// 현재 배정 찾기
		ReadingAssignment currentAssignment = allAssignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
			.filter(a -> a.getCycleNumber().equals(currentCycle))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), currentAssignment.getEndDate());
		if (daysRemaining < 0) {
			daysRemaining = 0;
		}

        // 1. 현재 읽고 있는 책의 Progress 정보 조회
        Progress progress = progressRepository
                .findByMemberAndBook(member, currentAssignment.getBook())
                .orElse(null); // Progress가 없을 수도 있으므로 null 허용

        Integer currentPage = (progress != null) ? progress.getCurrentPage() : 0;
        Float progressPercent = (progress != null) ? progress.getCurrentProgressPercentage() : 0.0f;


        // ... (앞으로 읽을 책들 조회 로직) ...
        List<CurrentReadingBookResponse.UpcomingBook> upcomingBooks = allAssignments.stream()
                .filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
                .filter(a -> a.getCycleNumber() > currentCycle)
                .sorted((a1, a2) -> a1.getCycleNumber().compareTo(a2.getCycleNumber()))
                .map(a -> CurrentReadingBookResponse.UpcomingBook.builder()
                        .bookId(a.getBook().getBookId())
                        .coverImageUrl(a.getBook().getCoverUrl())
                        .cycleNumber(a.getCycleNumber())
                        .build())
                .collect(Collectors.toList());

        // 2. DTO 빌더에 진행도 필드 추가
        return CurrentReadingBookResponse.builder()
                .bookId(currentAssignment.getBook().getBookId())
                .coverImageUrl(currentAssignment.getBook().getCoverUrl())
                .daysRemaining(daysRemaining)
                .nextExchangeDate(currentAssignment.getEndDate())
                .currentPage(currentPage)      // 추가된 현재 페이지
                .progressPercent(progressPercent) // 추가된 진행률
                .upcomingBooks(upcomingBooks)
                .build();
	}

	/**
	 * 다음에 읽을 책 정보 조회
	 *
	 * @param userId 사용자 ID
	 * @param groupId 모임 ID
	 * @return 다음에 읽을 책 정보 (표지, 읽을 수 있는 날짜, 현재 독자, 책 정보, 한줄평들)
	 * @throws BusinessException G001, M001, A001
	 */
	@Override
	public NextReadingBookResponse getNextReadingBook(String userId, Long groupId) {
		groupRepository.findById(groupId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GROUP_NOT_FOUND));

		Member member = memberRepository.findByGroup_GroupIdAndUserId(groupId, userId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		List<ReadingAssignment> allAssignments = assignmentRepository.findByGroup_GroupId(groupId);

		Integer currentCycle = getCurrentCycle(allAssignments);

		Integer nextCycle = currentCycle + 1;

		// 다음 회차에서 내가 읽을 책 찾기
		ReadingAssignment nextAssignment = allAssignments.stream()
			.filter(a -> a.getMember().getMemberId().equals(member.getMemberId()))
			.filter(a -> a.getCycleNumber().equals(nextCycle))
			.findFirst()
			.orElseThrow(() -> new BusinessException(ErrorCode.ASSIGNMENT_NOT_FOUND));

		Book nextBook = nextAssignment.getBook();

		// 현재 회차에서 이 책을 읽고 있는 사람 찾기
		ReadingAssignment currentReaderAssignment = allAssignments.stream()
			.filter(a -> a.getCycleNumber().equals(currentCycle))
			.filter(a -> a.getBook().getBookId().equals(nextBook.getBookId()))
			.findFirst()
			.orElse(null);

		String currentReaderNickname = currentReaderAssignment != null
			? currentReaderAssignment.getMember().getNickname()
			: "독서중인 모임원 없음";

		// 이 책에 대한 모임원들의 한줄평 찾기 (모든 회차들에서)
		List<NextReadingBookResponse.BookReview> reviews = allAssignments.stream()
			.filter(a -> a.getBook().getBookId().equals(nextBook.getBookId()))
			.filter(a -> a.getReview() != null && !a.getReview().isEmpty())
			.map(a -> NextReadingBookResponse.BookReview.builder()
				.memberId(a.getMember().getMemberId())
				.nickname(a.getMember().getNickname())
				.color(a.getMember().getColor())
				.review(a.getReview())
				.build())
			.collect(Collectors.toList());

		return NextReadingBookResponse.builder()
			.bookId(nextBook.getBookId())
			.coverImageUrl(nextBook.getCoverUrl())
			.bookTitle(nextBook.getTitle())
			.author(nextBook.getAuthor())
			.genre(nextBook.getGenre())
			.description(nextBook.getDescription())
			.availableFrom(nextAssignment.getStartDate())
			.currentReaderNickname(currentReaderNickname)
			.reviews(reviews)
			.build();
	}
}
