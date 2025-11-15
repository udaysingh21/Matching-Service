package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.model.VolunteerRequest;
import com.ngo.matching.repository.PostingRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class MatchingService {

    private final PostingRepository repo;

    public MatchingService(PostingRepository repo) {
        this.repo = repo;
    }

    // ---------------- ADD POSTING --------------------
    public PostingResponse addPosting(PostingResponse posting) {
        return repo.save(posting);
    }

    // ---------------- RECOMMEND POSTINGS -------------
    public List<PostingResponse> recommendPostings(VolunteerRequest req) {

        String location = (req.getLocation() != null && !req.getLocation().isEmpty())
                ? req.getLocation()
                : null;

        LocalDate date = req.getDate(); // null means no date filter

        if (location != null && date != null) {
            return repo.findByLocationIgnoreCaseAndDateAndSlotsAvailableGreaterThan(
                    location, date, 0
            );
        }

        if (location != null) {
            return repo.findByLocationIgnoreCaseAndSlotsAvailableGreaterThan(
                    location, 0
            );
        }

        if (date != null) {
            return repo.findByDateAndSlotsAvailableGreaterThan(
                    date, 0
            );
        }

        return repo.findBySlotsAvailableGreaterThan(0);
    }


    // ---------------- LOCK POSTING -------------------
    public String lockPosting(Long volunteerId, Long postingId) {

        PostingResponse posting = repo.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found"));

        if (posting.getSlotsAvailable() <= 0) {
            throw new RuntimeException("No slots available");
        }

        // reduce slot
        posting.setSlotsAvailable(posting.getSlotsAvailable() - 1);
        repo.save(posting);

        return "Volunteer " + volunteerId + " successfully locked posting " + postingId;
    }

}
