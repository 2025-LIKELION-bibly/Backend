package likelion.bibly.domain.member.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.enums.MemberStatus;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
	List<Member> findByGroup_GroupIdAndStatus(Long groupId, MemberStatus status);
	boolean existsByGroup_GroupIdAndNicknameAndStatus(Long groupId, String nickname, MemberStatus status);
	boolean existsByGroup_GroupIdAndColorAndStatus(Long groupId, String color, MemberStatus status);
	long countByGroup_GroupIdAndStatus(Long groupId, MemberStatus status);
	Optional<Member> findByGroup_GroupIdAndUserId(Long groupId, String userId);
	long countByUserIdAndStatus(String userId, MemberStatus status);
	List<Member> findByUserIdAndStatus(String userId, MemberStatus status);
}