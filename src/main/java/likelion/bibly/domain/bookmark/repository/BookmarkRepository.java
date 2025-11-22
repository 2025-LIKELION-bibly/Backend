package likelion.bibly.domain.bookmark.repository;

import likelion.bibly.domain.bookmark.entity.Bookmark;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.session.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    /**
     * 특정 세션에 속한 모든 북마크 이력을 생성 시간 역순으로 조회
     */
    //List<Bookmark> findByReadingsessionOrderByCreatedAtDesc(ReadingSession session, Member member);

    /**
     * 특정 멤버가 특정 세션에서 생성한 모든 북마크 이력을 조회
     */
    List<Bookmark> findByReadingsessionAndMemberOrderByCreatedAtDesc(ReadingSession session, Member member);

    /**
     * 특정 멤버가 생성한 가장 최근의 북마크 이력을 조회
     */

    Optional<Bookmark> findTopByReadingsessionAndMemberOrderByCreatedAtDesc(ReadingSession session, Member sessionMember);
}