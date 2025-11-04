package com.ngo.matching.controller;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.model.VolunteerRequest;
import com.ngo.matching.service.MatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

@Tag(name = "Matching API", description = "Recommend NGO postings & lock assignments")
@RestController
@RequestMapping("/api/matching")
public class MatchingController {

    @Autowired
    private MatchingService matchingService;

    @Operation(summary = "Recommend NGO postings for a volunteer")
    @PostMapping("/recommend")
    public List<PostingResponse> recommendPostings(@RequestBody VolunteerRequest request) {
        return matchingService.recommendPostings(request);
    }

    @Operation(summary = "Lock posting for volunteer (reserve slot)")
    @PostMapping("/lock/{volunteerId}/{postingId}")
    public String lockPosting(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId) {

        return matchingService.lockPosting(volunteerId, postingId);
    }
}
