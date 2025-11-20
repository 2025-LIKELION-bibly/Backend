package likelion.bibly.domain.comment.service;

import likelion.bibly.domain.comment.dto.CommentCreateRequest;
import likelion.bibly.domain.comment.dto.CommentResponse;
import likelion.bibly.domain.comment.entity.Comment;
import likelion.bibly.domain.comment.enums.AnnotationType;
import likelion.bibly.domain.comment.repository.CommentRepository;
import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.highlight.repository.HighlightRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.global.exception.BusinessException; // 💡 BusinessException import
import likelion.bibly.global.exception.ErrorCode;       // 💡 ErrorCode import
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final MemberRepository memberRepository;
    private final HighlightRepository highlightRepository;
    private final CommentRepository commentRepository;

    private static final int MEMO_THRESHOLD = 25;

    @Transactional
    public CommentResponse createComment(CommentCreateRequest request) {

        // 404 (멤버 찾을 수 없음)
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 404 (하이라이트 찾을 수 없음)
        Highlight highlight = highlightRepository.findById(request.highlightId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 세션 정보는 Highlight 엔티티에서 가져옴
        ReadingSession session = highlight.getSession();

        Comment parentComment = null;
        if (request.parentCommentId() != null) {
            // 404 (부모 코멘트 찾을 수 없음)
            parentComment = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

            // 400 (부모-자식 코멘트의 하이라이트/세션 일치 여부 검사)
            if (!parentComment.getHighlight().getHighlightId().equals(request.highlightId())) {
                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
            }
        }

        // content 길이에 따라 타입 결정(25자 이하면 코멘트, 초과하면 메모)
        AnnotationType determinedType;
        if (request.content().length() > MEMO_THRESHOLD) {
            determinedType = AnnotationType.MEMO;
        } else {
            determinedType = AnnotationType.COMMENT;
        }

        // Comment 엔티티 생성
        Comment comment = Comment.builder()
                .highlight(highlight) // 참조
                .member(member)
                .session(session)
                .content(request.content())
                .visibility(request.visibility())
                .annotationType(determinedType)
                .parentComment(parentComment) // 💡 parentComment 추가 (엔티티 수정 가정)
                .build();

        // 저장 및 응답 반환
        commentRepository.save(comment);
        return new CommentResponse(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, Long currentMemberId) {
        // 404 (코멘트 찾을 수 없음)
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 403 (작성자 권한 확인)
        if (!comment.getMember().getMemberId().equals(currentMemberId)) { // Member 엔티티의 getId() 사용
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 409 (자식 코멘트 존재 여부 확인)
        if (!comment.getChildComments().isEmpty()) {
            throw new BusinessException(ErrorCode.DELETE_TRACE_CONFLICT);
        }

        // 삭제 진행
        commentRepository.delete(comment);
    }
}