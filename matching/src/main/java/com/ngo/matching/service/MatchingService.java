package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.repository.PostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    // LOCK POSTING (FULL WORKFLOW)
    // ------------------------------------------

    // Change method signature to accept Authorization header
    public String lockPosting(Long volunteerId, Long postingId, String authHeader) {
        PostingResponse posting = repo.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found"));

        if (posting.getVolunteersNeeded() <= 0) {
            throw new RuntimeException("No volunteer slots left!");
        }

        // Volunteer MS check
        Boolean hasApplied = restTemplate.getForObject(
                VOL_MS + "/hasApplied/" + volunteerId + "/" + postingId,
                Boolean.class
        );
        if (Boolean.TRUE.equals(hasApplied)) {
            throw new RuntimeException("Volunteer already applied for this posting");
        }

        // Save in Volunteer MS
        String volApply = restTemplate.postForObject(
                VOL_MS + "/apply/" + volunteerId + "/" + postingId,
                null,
                String.class
        );
        if (volApply == null || volApply.toLowerCase().contains("error")) {
            throw new RuntimeException("Volunteer MS failed to save application");
        }

        // Save in Posting MS with Authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authHeader);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> postAssignResponse = restTemplate.exchange(
                POST_MS + "/postings/" + postingId + "/register/" + volunteerId,
                HttpMethod.POST,
                entity,
                String.class
        );
        String postAssign = postAssignResponse.getBody();
        if (postAssign == null || postAssign.toLowerCase().contains("error")) {
            throw new RuntimeException("Posting MS failed to assign posting");
        }

        // Reduce local slot
        posting.setVolunteersNeeded(posting.getVolunteersNeeded() - 1);
        repo.save(posting);

        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
    }

}
