package likelion.bibly.domain.assignment.repository;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingAssignmentRepository extends JpaRepository<ReadingAssignment, Long> {

    List<ReadingAssignment> findByGroup_GroupId(Long groupId);

    Optional<ReadingAssignment> findByGroup_GroupIdAndCycleNumberOrderByCreatedAtDesc(Long groupId, Integer cycleNumber);

    List<ReadingAssignment> findByMember_MemberId(Long memberId);

    void deleteByGroup_GroupId(Long groupId);

    /**
     * 그룹 ID로 해당 그룹의 가장 최근 독서 할당을 조회합 (세션 종료 로직에서 필요)
     */
    Optional<ReadingAssignment> findTopByGroup_GroupIdOrderByCreatedAtDesc(Long groupId);
}