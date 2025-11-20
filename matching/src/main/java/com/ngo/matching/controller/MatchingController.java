package com.ngo.matching.controller;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.service.MatchingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching API")
public class MatchingController {

    @Autowired
    private MatchingService service;

    @Operation(summary = "Add a new posting")
    @PostMapping("/add")
    public ResponseEntity<PostingResponse> add(@RequestBody PostingResponse posting) {
        return ResponseEntity.ok(service.addPosting(posting));
    }

    @Operation(summary = "Recommend postings with optional filters")
    @GetMapping("/recommend")
    public ResponseEntity<?> recommend(
            @RequestParam(required = false) String pincode,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {

        // Validation: require at least 1 filter
        if ((pincode == null || pincode.isBlank()) &&
                (domain == null || domain.isBlank()) &&
                date == null) {
            return ResponseEntity
                    .badRequest()
                    .body("At least one filter (pincode/domain/date) is required.");
        }

        // Step 1: fetch all postings from posting-service
        List<PostingResponse> allPostings = service.fetchAllPostingsFromPostingService();

        // Step 2: filter in matching service
        List<PostingResponse> filtered = service.filterPostings(allPostings, pincode, domain, date);

        if (filtered.isEmpty()) {
            return ResponseEntity.status(404).body("No postings found for given filters.");
        }

        return ResponseEntity.ok(filtered);
    }


    @Operation(summary = "Lock posting for a volunteer")
    @PostMapping("/lock/{volunteerId}/{postingId}")
    public ResponseEntity<String> lock(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId,
            @RequestHeader("Authorization") String authHeader) {

        return ResponseEntity.ok(service.lockPosting(volunteerId, postingId, authHeader));
    }

}
