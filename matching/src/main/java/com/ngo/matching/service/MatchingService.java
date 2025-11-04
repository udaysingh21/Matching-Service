package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.model.VolunteerRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final List<PostingResponse> postings = List.of(
            new PostingResponse(1L, "NGO A", "Mumbai", LocalDate.of(2025, 1, 20), 3, false),
            new PostingResponse(2L, "NGO B", "Pune", LocalDate.of(2025, 1, 22), 1, false),
            new PostingResponse(3L, "NGO C", "Mumbai", LocalDate.of(2025, 1, 25), 0, false)
    );

    public List<PostingResponse> recommendPostings(VolunteerRequest req) {
        return postings.stream()
                .filter(p -> p.getLocation().equalsIgnoreCase(req.getLocation()))
                .filter(p -> !p.getDate().isBefore(req.getAvailableFrom()) && !p.getDate().isAfter(req.getAvailableTo()))
                .filter(p -> p.getSlotsAvailable() > 0)
                .map(p -> new PostingResponse(p.getPostingId(), p.getNgoName(), p.getLocation(), p.getDate(), p.getSlotsAvailable(), true))
                .collect(Collectors.toList());
    }

    public String lockPosting(Long volunteerId, Long postingId) {
        return "Volunteer " + volunteerId + " temporarily matched to Posting " + postingId + " (mock)";
    }
}
