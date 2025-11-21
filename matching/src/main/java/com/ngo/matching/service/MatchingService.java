package com.ngo.matching.service;

import com.ngo.matching.dto.PostingPageResponse;
import com.ngo.matching.dto.VolunteerPostingsResponse;
import com.ngo.matching.exception.*;
import com.ngo.matching.model.PostingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://localhost:8080}")
    private String userServiceUrl;

    @Value("${posting.service.url:http://localhost:8082}")
    private String postingServiceUrl;

    // Recommend postings with filters (cached)
    @Cacheable(
            value = "recommendationCache",
            key = "T(java.util.Objects).hash(#volunteerId, #location, #domain, #date)"
    )
    public List<PostingResponse> recommendPostings(Long volunteerId, String location, String domain, LocalDate date, String jwtToken) {
        log.info("Fetching recommendations for volunteer {} with filters: location={}, domain={}, date={}",
                volunteerId, location, domain, date);

        Set<Long> registeredPostingIds = getVolunteerRegisteredPostings(volunteerId, jwtToken);
        log.debug("Volunteer {} already registered for {} postings: {}", volunteerId, registeredPostingIds.size(), registeredPostingIds);

        List<PostingResponse> allPostings = fetchAllPostingsFromService(jwtToken);
        log.debug("Fetched {} postings from Posting Service", allPostings.size());

        List<PostingResponse> filteredPostings = allPostings.stream()
                .filter(p -> p.getVolunteersSpotLeft() != null && p.getVolunteersSpotLeft() > 0) // Has slots
                .filter(p -> !registeredPostingIds.contains(p.getId())) // Not already registered
                .filter(p -> location == null || location.isBlank() ||
                        (p.getLocation() != null && p.getLocation().equalsIgnoreCase(location)))
                .filter(p -> domain == null || domain.isBlank() ||
                        (p.getDomain() != null && p.getDomain().equalsIgnoreCase(domain)))
                .filter(p -> date == null ||
                        (p.getStartDate() != null && p.getStartDate().isEqual(date)))
                .collect(Collectors.toList());

        log.info("Found {} matching postings for volunteer {}", filteredPostings.size(), volunteerId);
        return filteredPostings;
    }

    // Register volunteer for posting
    @CacheEvict(value = "recommendationCache", allEntries = true)
    public String registerVolunteerForPosting(Long volunteerId, Long postingId, String jwtToken) {
        log.info("🔒 Attempting to register volunteer {} for posting {}", volunteerId, postingId);

        Set<Long> registeredPostings = getVolunteerRegisteredPostings(volunteerId, jwtToken);
        if (registeredPostings.contains(postingId)) {
            log.warn("Volunteer {} already registered for posting {}", volunteerId, postingId);
            throw new AlreadyRegisteredException("Volunteer " + volunteerId + " is already registered for posting " + postingId);
        }

        PostingResponse posting = getPostingById(postingId, jwtToken);
        if (posting.getVolunteersSpotLeft() == null || posting.getVolunteersSpotLeft() <= 0) {
            log.warn("No spots left for posting {}", postingId);
            throw new NoSlotsAvailableException("No slots available for posting " + postingId);
        }

        registerVolunteerInPostingService(volunteerId, postingId, jwtToken);
        log.info("Registered volunteer {} in Posting Service for posting {}", volunteerId, postingId);

        registerVolunteerInUserService(volunteerId, postingId, jwtToken);
        log.info("Registered volunteer {} in User Service for posting {}", volunteerId, postingId);

        log.info("Successfully registered volunteer {} for posting {}", volunteerId, postingId);
        return "Volunteer " + volunteerId + " successfully registered for posting " + postingId;
    }

    // Unregister volunteer from posting
    @CacheEvict(value = "recommendationCache", allEntries = true)
    public String unregisterVolunteerFromPosting(Long volunteerId, Long postingId, String jwtToken) {
        log.info("🔓 Attempting to unregister volunteer {} from posting {}", volunteerId, postingId);

        Set<Long> registeredPostings = getVolunteerRegisteredPostings(volunteerId, jwtToken);
        if (!registeredPostings.contains(postingId)) {
            log.warn("Volunteer {} not registered for posting {}", volunteerId, postingId);
            throw new NotRegisteredException("Volunteer " + volunteerId + " is not registered for posting " + postingId);
        }

        unregisterVolunteerInPostingService(volunteerId, postingId, jwtToken);
        log.info("Unregistered volunteer {} in Posting Service for posting {}", volunteerId, postingId);

        unregisterVolunteerInUserService(volunteerId, postingId, jwtToken);
        log.info("Unregistered volunteer {} from User Service for posting {}", volunteerId, postingId);

        log.info("Successfully unregistered volunteer {} from posting {}", volunteerId, postingId);
        return "Volunteer " + volunteerId + " successfully unregistered from posting " + postingId;
    }

    // PRIVATE HELPER METHODS

    private Set<Long> getVolunteerRegisteredPostings(Long volunteerId, String jwtToken) {
        String url = userServiceUrl + "/api/v1/users/volunteers/" + volunteerId + "/postings";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<VolunteerPostingsResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    VolunteerPostingsResponse.class
            );
            List<Long> postingIds = response.getBody() != null ? response.getBody().getPostings() : null;
            return postingIds == null ? Collections.emptySet() : new HashSet<>(postingIds);
        } catch (HttpClientErrorException.NotFound e) {
            throw new VolunteerNotFoundException("Volunteer not found: " + volunteerId);
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to fetch volunteer data from User Service");
        }
    }

    private List<PostingResponse> fetchAllPostingsFromService(String jwtToken) {
        String url = postingServiceUrl + "/api/v1/postings";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PostingPageResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PostingPageResponse.class
            );
            return response.getBody() != null ? response.getBody().getContent() : Collections.emptyList();
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to fetch postings from Posting Service");
        }
    }

    private PostingResponse getPostingById(Long postingId, String jwtToken) {
        String url = postingServiceUrl + "/api/v1/postings/" + postingId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<PostingResponse> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    PostingResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException.NotFound e) {
            log.error("Posting {} not found in Posting Service", postingId);
            throw new PostingNotFoundException("Posting not found: " + postingId);
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to fetch posting from Posting Service");
        }
    }

    private void registerVolunteerInPostingService(Long volunteerId, Long postingId, String jwtToken) {
        String url = postingServiceUrl + "/api/v1/postings/" + postingId + "/register/" + volunteerId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
            log.debug("Successfully registered volunteer {} in Posting Service", volunteerId);
        } catch (HttpClientErrorException.BadRequest e) {
            log.error("Volunteer already registered or no slots left in Posting Service");
            throw new AlreadyRegisteredException("Volunteer already registered or no slots available");
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to register volunteer in Posting Service");
        }
    }

    private void unregisterVolunteerInPostingService(Long volunteerId, Long postingId, String jwtToken) {
        String url = postingServiceUrl + "/api/v1/postings/" + postingId + "/unregister/" + volunteerId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
            log.debug("Successfully unregistered volunteer {} from Posting Service", volunteerId);
        } catch (Exception e) {
            throw new ServiceCommunicationException("Failed to unregister volunteer from Posting Service");
        }
    }

    private void registerVolunteerInUserService(Long volunteerId, Long postingId, String jwtToken) {
        String url = userServiceUrl + "/api/v1/users/volunteers/" + volunteerId + "/postings/" + postingId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);
    }

    private void unregisterVolunteerInUserService(Long volunteerId, Long postingId, String jwtToken) {
        String url = userServiceUrl + "/api/v1/users/volunteers/" + volunteerId + "/postings/" + postingId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        restTemplate.exchange(url, HttpMethod.DELETE, entity, Void.class);
    }
}
