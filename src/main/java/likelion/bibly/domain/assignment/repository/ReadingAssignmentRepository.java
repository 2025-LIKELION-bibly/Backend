package likelion.bibly.domain.assignment.repository;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReadingAssignmentRepository extends JpaRepository<ReadingAssignment, Long> {

    List<ReadingAssignment> findByGroup_GroupId(Long groupId);

    Optional<ReadingAssignment> findByGroup_GroupIdAndCycleNumberOrderByCreatedAtDesc(Long groupId, Integer cycleNumber);

    List<ReadingAssignment> findByMember_MemberId(Long memberId);

    void deleteByGroup_GroupId(Long groupId);
}