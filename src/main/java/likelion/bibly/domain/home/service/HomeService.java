package likelion.bibly.domain.home.service;

import likelion.bibly.domain.assignment.repository.ReadingAssignmentRepository;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.progress.repository.ProgressRepository;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final MemberRepository memberRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReadingAssignmentRepository readingAssignmentRepository;
    private final ProgressRepository progressRepository; // 진행도 조회를 위해 필요 (가정)
    // private final ReviewRepository reviewRepository;     // 한줄평 조회를 위해 필요 (가정)

    // ... (getHomeData 메서드 생략, 로직은 아래 헬퍼 메서드 사용)

    // --- Private 헬퍼 메서드: 그룹/멤버 조회 로직 수정 ---

//    /** E.1.2 모임 구성원 닉네임 첫 글자 조회 */
//    private List<String> getGroupMemberNicknames(Long groupId) {
//        // MemberRepository에 findByGroupIdOrderByJoinedAt(Long groupId) 같은 메서드가 있다고 가정
//        // joinedAt 필드가 Member 엔티티에 있다고 가정합니다.
//        return memberRepository.findByGroupId(groupId).stream()
//                // .sorted(Comparator.comparing(Member::getJoinedAt)) // 필요하다면 정렬 로직 추가
//                .map(member -> member.getNickname().substring(0, 1))
//                .collect(Collectors.toList());
//    }
//
//    // --- 핵심 섹션 로직 (E.1.4, E.1.5) ---
//
//    // E.1.4: 현재 읽는 책 정보 설정
//    private void populateCurrentBookSection(HomeResponse.HomeResponseBuilder builder, Group group, ReadingSession currentSession) {
//
//        // 1. 현재 읽는 책 정보
//        builder.currentSessionId(currentSession.getSessionId())
//                .currentBookId(currentSession.getBook().getBookId())
//                .currentBookInfo(new BookSimpleResponse(currentSession.getBook()));
//
//        // 2. 진행도 표시 (사용자가 읽은 페이지 중 가장 뒷페이지)
//        // ReadingSession 엔티티 자체에 단일 북마크 페이지(bookMarkPage)가 저장되어 있다고 가정합니다.
//        Integer latestPage = currentSession.getBookMarkPage() != null ? currentSession.getBookMarkPage() : 0;
//
//        // 3. 교환 독서일 및 남은 기간 표시
//        readingAssignmentRepository
//                .findBySession(currentSession) // Assignment가 Session과 연결되어 있다고 가정
//                .ifPresent(assignment -> {
//                    LocalDate nextExchangeDate = assignment.getEndDate();
//                    long days = ChronoUnit.DAYS.between(LocalDate.now(), nextExchangeDate);
//
//                    builder.exchangeDday("D-" + days) // 교환독서일까지 남은 기간
//                            .nextExchangeDate(nextExchangeDate); // 다음 교환독서일
//                });
//
//        // 4. 진행도 퍼센트 및 최신 페이지는 클라이언트가 별도 API (/api/v1/sessions/member/{memberId})를 통해 가져와서 표시
//        // HomeResponse DTO에는 progressPercent 필드를 추가하지 않았으므로, 2차 호출 시 사용
//    }
//
//    // E.1.5: 다음에 읽을 책 정보 설정
//    private void populateNextBookSection(HomeResponse.HomeResponseBuilder builder, Group group, ReadingSession currentSession) {
//
//        // 1. 현재 세션 다음 순서의 Assignment 조회
//        // Repository 메서드 가정: 현재 세션 이후의 다음 순서 할당 조회
//        readingAssignmentRepository.findNextAssignment(group, currentSession)
//                .ifPresent(assignment -> {
//
//                    // 2. 다음 책 정보 (섬네일, 책 제목, 저자, 장르, 책소개)
//                    builder.nextBookInfo(new BookSimpleResponse(assignment.getBook()));
//
//                    // 3. 책을 언제부터 읽을 수 있는지 표시
//                    builder.nextReadStartDate(String.format("%d월 %d일부터 읽을 수 있어요",
//                            assignment.getStartDate().getMonthValue(),
//                            assignment.getStartDate().getDayOfMonth()));
//
//                    // 4. 이미 읽은 사람들의 아이콘/닉네임 표시
//                    List<MemberInfoResponse> readers = readingSessionRepository
//                            .findCompletedMembersForBookInGroup(assignment.getBook(), group).stream() // Repository 메서드 가정
//                            .map(m -> MemberInfoResponse.builder().nickname(m.getNickname()).build())
//                            .collect(Collectors.toList());
//
//                    // 5. 이미 읽은 사람들의 한줄평
//                    // ReviewRepository 사용 가정
//                    List<String> reviews = List.of("재미있어요!", "생각보다 어려웠습니다."); // 임시 데이터 혹은 reviewRepository.findTop3ByBook(assignment.getBook())
//
//                    builder.readers(readers)
//                            .latestReviews(reviews);
//                });
    }
//}