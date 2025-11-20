package likelion.bibly.domain.highlight.service;

import likelion.bibly.domain.comment.repository.CommentRepository;
import likelion.bibly.domain.highlight.dto.HighlightCreateRequest;
import likelion.bibly.domain.highlight.dto.HighlightResponse;
import likelion.bibly.domain.highlight.entity.Highlight;
import likelion.bibly.domain.highlight.repository.HighlightRepository;
import likelion.bibly.domain.member.entity.Member;
import likelion.bibly.domain.member.repository.MemberRepository;
import likelion.bibly.domain.session.entity.ReadingSession;
import likelion.bibly.domain.session.repository.ReadingSessionRepository;
import likelion.bibly.global.exception.BusinessException;
import likelion.bibly.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HighlightService {

    private final HighlightRepository highlightRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final MemberRepository memberRepository;
    private final CommentRepository commentRepository;

    /**
     * H.1 하이라이트 생성
     */
    @Transactional
    public HighlightResponse createHighlight(HighlightCreateRequest request) {
        // 404 (세션 찾을 수 없음)
        ReadingSession session = readingSessionRepository.findById(request.sessionId())
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 404 (멤버 찾을 수 없음)
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 400 (유효하지 않은 오프셋)
        if (request.startOffset() == null || request.endOffset() == null ||
                request.startOffset() < 0 || request.endOffset() < request.startOffset()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // Highlight 엔티티 생성
        Highlight highlight = Highlight.builder()
                .session(session)
                .member(member)
                .textSentence(request.textSentence())
                .color(request.color())
                .highlightedPage(request.highlightedPage())
                .startOffset(request.startOffset())
                .endOffset(request.endOffset())
                .build();

        // 저장 및 응답 반환
        highlightRepository.save(highlight);

        return new HighlightResponse(highlight, Collections.emptyList());
    }

    /**
     * H.2 하이라이트 삭제
     */
    @Transactional
    public void deleteHighlight(Long highlightId, Long currentMemberId) {
        // 404 (하이라이트 찾을 수 없음)
        Highlight highlight = highlightRepository.findById(highlightId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 403 (권한 없음)
        if (!highlight.getMember().getMemberId().equals(currentMemberId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        // 409 (코멘트 연결됨)
        boolean hasAttachedComments = commentRepository.existsByHighlightHighlightId(highlightId);

        if (hasAttachedComments) {
            throw new BusinessException(ErrorCode.DELETE_TRACE_CONFLICT);
        }

        // 삭제 진행
        highlightRepository.delete(highlight);
    }
}