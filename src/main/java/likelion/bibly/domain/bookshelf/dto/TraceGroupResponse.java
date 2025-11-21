package likelion.bibly.domain.bookshelf.dto;

import java.util.List;

public record TraceGroupResponse(
        String progressRange, // "0% ~ 10%", "10% ~ 20%"
        int count,
        List<TraceItemResponse> traces 
) {}