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

    @Operation(summary = "Recommend postings with optional filters")
    @GetMapping("/recommend")
    public ResponseEntity<List<PostingResponse>> recommend(
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        // Check if all inputs are null/empty
        if ((location == null || location.isBlank()) &&
                (domain == null || domain.isBlank()) &&
                date == null) {
            return ResponseEntity
                    .badRequest()
                    .body(null); // or throw a custom exception
        }

        List<PostingResponse> postings = service.recommendPostings(location, domain, date);

        if (postings == null || postings.isEmpty()) {
            return ResponseEntity.noContent().build(); // 204 No Content if no postings found
        }

        return ResponseEntity.ok(postings);
    }



    @Operation(summary = "Lock posting for a volunteer")
    @PostMapping("/lock/{volunteerId}/{postingId}")
    public ResponseEntity<String> lock(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId) {

        return ResponseEntity.ok(service.lockPosting(volunteerId, postingId));
    }
}
