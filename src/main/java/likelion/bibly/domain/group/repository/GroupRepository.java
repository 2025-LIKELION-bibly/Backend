package likelion.bibly.domain.group.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import likelion.bibly.domain.group.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
	Optional<Group> findByInviteCode(String inviteCode);
	boolean existsByInviteCode(String inviteCode);
}
