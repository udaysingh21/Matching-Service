package com.ngo.matching.dto;

import com.ngo.matching.model.PostingResponse;
import lombok.Data;

import java.util.List;

@Data
public class PostingPageResponse {
    private List<PostingResponse> content;

    // Pagination metadata fields (if needed)
    private int totalElements;
    private int totalPages;
    private boolean last;
    private int size;
    private int number;
    private boolean first;
    private int numberOfElements;

    // Optional sorting details omitted for brevity
}
