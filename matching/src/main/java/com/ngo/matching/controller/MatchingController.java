package com.ngo.matching.controller;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.model.VolunteerRequest;
import com.ngo.matching.service.MatchingService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/matching")
@Tag(name = "Matching API")
public class MatchingController {

    @Autowired
    private MatchingService service;

    @Operation(summary = "Add a new posting")
    @PostMapping("/add")
    public ResponseEntity<PostingResponse> add(@RequestBody PostingResponse posting) {
        return ResponseEntity.ok(service.addPosting(posting));
    }

    @Operation(summary = "Recommend postings")
    @PostMapping("/recommend")
    public ResponseEntity<List<PostingResponse>> recommend(@RequestBody VolunteerRequest req) {
        return ResponseEntity.ok(service.recommendPostings(req));
    }

    @Operation(summary = "Lock posting for a volunteer")
    @PostMapping("/lock/{volunteerId}/{postingId}")
    public ResponseEntity<String> lock(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId) {

        return ResponseEntity.ok(service.lockPosting(volunteerId, postingId));
    }
}
