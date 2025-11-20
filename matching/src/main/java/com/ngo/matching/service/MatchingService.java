package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
//import com.ngo.matching.repository.PostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import java.util.Map;
import java.util.Collections;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class MatchingService {

//    @Autowired
//    private PostingRepository repo;

    @Autowired
    private RestTemplate restTemplate;

    private final String VOL_MS = "http://localhost:8080/api/volunteer";  // using mock
    private final String POST_MS = "http://localhost:8080/api/posting";    // using mock

    // ------------------------------------------
//    // ADD POSTING (cache must reset)
//    // ------------------------------------------
//    @CacheEvict(value = "recommendationCache", allEntries = true)
//    public PostingResponse addPosting(PostingResponse posting) {
//        return repo.save(posting);
//    }

    // ------------------------------------------
    // RECOMMEND POSTINGS
    // ------------------------------------------
    @Cacheable(
            value = "recommendationCache",
            key = "#pincode + '-' + #domain + '-' + (#date != null ? #date.toString() : 'null')"
    )
    public List<PostingResponse> recommendPostings(
            String pincode,
            String domain,
            LocalDate date
    ) {

        System.out.println("🔵 Cache MISS → Fetching from Posting MS...");

        // Call Posting MS for all postings
        String url = POST_MS + "/postings?page=0&size=200&sortBy=createdAt&sortDir=DESC";

        // Type-safe deserialization into PostingResponse[]
        PostingResponse[] response = restTemplate.getForObject(url, PostingResponse[].class);

        if (response == null || response.length == 0) {
            return Collections.emptyList();
        }

        List<PostingResponse> postings = Arrays.asList(response);

        // Apply filters: pincode, domain, date, and check volunteersSpotLeft
        return postings.stream()
                .filter(p -> p.getVolunteersSpotLeft() != null && p.getVolunteersSpotLeft() > 0)
                .filter(p -> match(pincode, p.getPincode()))
                .filter(p -> match(domain, p.getDomain()))
                .filter(p -> dateMatch(date, p.getStartDate(), p.getEndDate()))
                .limit(10)
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---
    private boolean match(String filter, String actual) {
        return filter == null || filter.isBlank() || (actual != null && actual.equalsIgnoreCase(filter));
    }

    private boolean dateMatch(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return true;
        if (start == null || end == null) return true;
        // inclusive range check
        return !date.isBefore(start) && !date.isAfter(end);
    }

    //------------------------------------------
        //LOCKPOSTING - FUNCTION
    // ------------------------------------------
//    public String lockPosting(Long volunteerId, Long postingId, String authHeader) {
//
//        // 1. Validate posting exists locally (optional if your MS stores posting)
//        PostingResponse posting = repo.findById(postingId)
//                .orElseThrow(() -> new RuntimeException("Posting not found"));
//
//        // --------------------------------------------
//        // STEP 1: CHECK IF VOLUNTEER ALREADY APPLIED
//        // --------------------------------------------
//
//        ResponseEntity<List> appliedPostingsResponse =
//                restTemplate.getForEntity(
//                        VOL_MS + "/api/v1/users/volunteers/" + volunteerId + "/postings",
//                        List.class
//                );
//
//        List<Long> alreadyAppliedPostings = appliedPostingsResponse.getBody();
//
//        if (alreadyAppliedPostings != null && alreadyAppliedPostings.contains(postingId)) {
//            throw new RuntimeException("Volunteer already registered for this posting");
//        }
//
//        // --------------------------------------------
//        // STEP 2: ASK POSTING MS TO REGISTER VOLUNTEER
//        // --------------------------------------------
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", authHeader);
//        HttpEntity<Void> entity = new HttpEntity<>(headers);
//
//        ResponseEntity<String> registerResponse = restTemplate.exchange(
//                POST_MS + "/api/v1/postings/" + postingId + "/register/" + volunteerId,
//                HttpMethod.POST,
//                entity,
//                String.class
//        );
//
//        if (registerResponse.getBody() == null ||
//                registerResponse.getBody().toLowerCase().contains("error")) {
//            throw new RuntimeException("Posting MS failed: No slots or error while registering");
//        }
//
//        // --------------------------------------------
//        // STEP 3: SAVE MAPPING IN VOLUNTEER MS
//        // --------------------------------------------
//
//        ResponseEntity<String> mappingResponse = restTemplate.postForEntity(
//                VOL_MS + "/api/v1/users/volunteers/" + volunteerId + "/postings/" + postingId,
//                null,
//                String.class
//        );
//
//        if (mappingResponse.getBody() == null ||
//                mappingResponse.getBody().toLowerCase().contains("error")) {
//
//            // ROLLBACK: Undo slot reservation
//            restTemplate.exchange(
//                    POST_MS + "/api/v1/postings/" + postingId + "/unregister/" + volunteerId,
//                    HttpMethod.POST,
//                    entity,
//                    String.class
//            );
//
//            throw new RuntimeException("Failed to save volunteer-posting mapping, rolled back");
//        }
//
//        // --------------------------------------------
//        // OPTIONAL: UPDATE LOCAL MATCHING MS STORAGE(must be done by posting -ms)
//        // --------------------------------------------
//        posting.setVolunteersNeeded(posting.getVolunteersNeeded() - 1);
//        repo.save(posting);
//
//        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
//    }
//
    public String lockPosting(Long volunteerId, Long postingId, String authHeader) {

        // STEP 1: ASK POSTING MS TO REGISTER VOLUNTEER
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

        // STEP 2: SAVE MAPPING IN VOLUNTEER MS
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

        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
    }
}
