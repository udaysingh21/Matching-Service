package com.ngo.matching.service;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.repository.PostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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


    // ------------------------------------------
    // ADD POSTING (cache must reset)
    // ------------------------------------------
    @CacheEvict(value = "recommendationCache", allEntries = true)
    public PostingResponse addPosting(PostingResponse posting) {
        return repo.save(posting);
    }


    // ------------------------------------------
    // RECOMMEND POSTINGS (CACHED)
    // ------------------------------------------
    @Cacheable(
            value = "recommendationCache",
            key = "T(java.util.Objects).hash(#location, #domain, #date)"
    )
    public List<PostingResponse> recommendPostings(String location, String domain, LocalDate date) {

        System.out.println("🔵 Fetching from DB (cache MISS)...");

        List<PostingResponse> all = repo.findAll();

        return all.stream()
                .filter(p -> location == null || p.getLocation().equalsIgnoreCase(location))
                .filter(p -> domain == null || p.getDomain().equalsIgnoreCase(domain))
                .filter(p -> date == null || p.getDate().isEqual(date))
                .filter(p -> p.getSlotsAvailable() > 0)
                .collect(Collectors.toList());
    }


    // ------------------------------------------
    // LOCK POSTING (reduces slot → cache must reset)
    // ------------------------------------------
    @CacheEvict(value = "recommendationCache", allEntries = true)
    public String lockPosting(Long volunteerId, Long postingId) {

        PostingResponse posting = repo.findById(postingId)
                .orElseThrow(() -> new RuntimeException("Posting not found"));

        if (posting.getSlotsAvailable() <= 0) {
            return "No slots left!";
        }

        posting.setSlotsAvailable(posting.getSlotsAvailable() - 1);
        repo.save(posting);

        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
    }
}
