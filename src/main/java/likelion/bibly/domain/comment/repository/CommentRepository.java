package likelion.bibly.domain.comment.repository;

import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.highlight.entity.Highlight;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // F4, F5 (상세보기, 흔적 모아보기)
    List<Comment> findByHighlightIn(List<Highlight> highlights);
}