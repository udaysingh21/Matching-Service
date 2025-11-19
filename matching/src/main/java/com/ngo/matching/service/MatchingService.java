package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.repository.PostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    @Autowired
    private PostingRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    private final String VOL_MS = "http://localhost:8080/api/volunteer";  // using mock
    private final String POST_MS = "http://localhost:8080/api/posting";    // using mock


//    private final String VOL_MS = "http://localhost:8081/api/volunteer";
//    private final String POST_MS = "http://localhost:8082/api/posting";

    // ------------------------------------------
    // ADD POSTING (cache must reset)
    // ------------------------------------------
    @CacheEvict(value = "recommendationCache", allEntries = true)
    public PostingResponse addPosting(PostingResponse posting) {
        return repo.save(posting);
    }

    // ------------------------------------------
    // RECOMMEND POSTINGS
    // ------------------------------------------
    @Cacheable(
            value = "recommendationCache",
            key = "#pincode + '-' + #domain + '-' + (#date != null ? #date.toString() : '')"
    )
    public List<PostingResponse> recommendPostings(String pincode, String domain, LocalDate date) {

        System.out.println("🔵 Fetching from DB (cache MISS)...");

        // fetch all postings with at least 1 volunteer
        List<PostingResponse> all = repo.findByVolunteersNeededGreaterThan(0);

        // apply optional filters: pincode, domain, date
        return all.stream()
                .filter(p -> p.getVolunteersNeeded() > 0) // internal check
                .filter(p -> pincode == null || p.getPincode().equalsIgnoreCase(pincode))
                .filter(p -> domain == null || p.getDomain().equalsIgnoreCase(domain))
                .filter(p -> date == null || (!p.getStartDate().isAfter(date) && !p.getEndDate().isBefore(date)))
                .collect(Collectors.toList());
    }

    // ------------------------------------------
    public String lockPosting(Long volunteerId, Long postingId, String authHeader) {

        // 1. Validate posting exists locally (optional if your MS stores posting)
        PostingResponse posting = repo.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found"));

        // --------------------------------------------
        // STEP 1: CHECK IF VOLUNTEER ALREADY APPLIED
        // --------------------------------------------

        ResponseEntity<List> appliedPostingsResponse =
                restTemplate.getForEntity(
                        VOL_MS + "/api/v1/users/volunteers/" + volunteerId + "/postings",
                        List.class
                );

        List<Long> alreadyAppliedPostings = appliedPostingsResponse.getBody();

        if (alreadyAppliedPostings != null && alreadyAppliedPostings.contains(postingId)) {
            throw new RuntimeException("Volunteer already registered for this posting");
        }

        // --------------------------------------------
        // STEP 2: ASK POSTING MS TO REGISTER VOLUNTEER
        // --------------------------------------------
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> registerResponse = restTemplate.exchange(
                POST_MS + "/api/v1/postings/" + postingId + "/register/" + volunteerId,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (registerResponse.getBody() == null ||
                registerResponse.getBody().toLowerCase().contains("error")) {
            throw new RuntimeException("Posting MS failed: No slots or error while registering");
        }

        // --------------------------------------------
        // STEP 3: SAVE MAPPING IN VOLUNTEER MS
        // --------------------------------------------

        ResponseEntity<String> mappingResponse = restTemplate.postForEntity(
                VOL_MS + "/api/v1/users/volunteers/" + volunteerId + "/postings/" + postingId,
                null,
                String.class
        );

        if (mappingResponse.getBody() == null ||
                mappingResponse.getBody().toLowerCase().contains("error")) {

            // ROLLBACK: Undo slot reservation
            restTemplate.exchange(
                    POST_MS + "/api/v1/postings/" + postingId + "/unregister/" + volunteerId,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            throw new RuntimeException("Failed to save volunteer-posting mapping, rolled back");
        }

        // --------------------------------------------
        // OPTIONAL: UPDATE LOCAL MATCHING MS STORAGE(must be done by posting -ms)
        // --------------------------------------------
        posting.setVolunteersNeeded(posting.getVolunteersNeeded() - 1);
        repo.save(posting);

        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
    }

}
