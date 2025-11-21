package com.ngo.matching.dto;

import lombok.Data;
import java.util.List;

@Data
public class VolunteerPostingsResponse {
    private int count;
    private List<Long> postings;
    private Long volunteerId;
}

