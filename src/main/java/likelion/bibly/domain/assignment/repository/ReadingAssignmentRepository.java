package likelion.bibly.domain.assignment.repository;

import likelion.bibly.domain.assignment.entity.ReadingAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReadingAssignmentRepository extends JpaRepository<ReadingAssignment, Long> {

    /**
     * * @param groupId 조회할 모임의 ID
     * @return 해당 모임의 ReadingAssignment 리스트
     */
    List<ReadingAssignment> findByGroup_GroupId(Long groupId);

    //
    // 여기에 ReadingAssignment 관련 다른 쿼리 메서드
}