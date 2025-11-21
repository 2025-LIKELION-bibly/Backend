package likelion.bibly.domain.comment.repository;

import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.highlight.entity.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByHighlightIn(List<Highlight> highlights);

    boolean existsByHighlightHighlightId(Long highlightId);

    @Query("""
        SELECT c FROM Comment c 
        JOIN c.highlight h 
        JOIN h.session s 
        JOIN s.book b 
        JOIN s.group g 
        WHERE b.bookId = :bookId AND g.groupId = :groupId
    """)
    List<Comment> findCommentsByBookIdAndGroupIdUsingJoins(
            @Param("bookId") Long bookId,
            @Param("groupId") Long groupId
    );
}

