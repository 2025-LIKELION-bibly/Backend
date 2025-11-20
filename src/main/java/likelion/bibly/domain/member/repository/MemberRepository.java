package likelion.bibly.domain.member.repository;

import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	List<Member> findByGroup_GroupIdAndStatus(Long groupId, MemberStatus status);
	boolean existsByGroup_GroupIdAndNicknameAndStatus(Long groupId, String nickname, MemberStatus status);
	boolean existsByGroup_GroupIdAndColorAndStatus(Long groupId, String color, MemberStatus status);
	long countByGroup_GroupIdAndStatus(Long groupId, MemberStatus status);
	Optional<Member> findByGroup_GroupIdAndUserId(Long groupId, String userId);
	long countByUserIdAndStatus(String userId, MemberStatus status);
	List<Member> findByUserIdAndStatus(String userId, MemberStatus status);

    @Query("SELECT m.id FROM Member m WHERE m.userId = :userId AND m.status = :status")
    List<Long> findActiveMemberIdsByUserId(@Param("userId") String userId, @Param("status") MemberStatus status);

    List<Member> findByGroup_GroupId(Long groupId);
}