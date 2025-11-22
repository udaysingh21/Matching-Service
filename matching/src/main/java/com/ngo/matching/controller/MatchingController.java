package com.ngo.matching.controller;

import com.ngo.matching.model.PostingResponse;
import com.ngo.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@CrossOrigin(
    origins = {"http://localhost:3000", "http://localhost:5173", "http://localhost:5174", "http://localhost:8080"},
    allowCredentials = "true",
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS}
)
@Slf4j
@RestController
@RequestMapping("/api/v1/matching")
@Tag(name = "Matching Service", description = "Volunteer-Posting Matching APIs")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    // =====================================================
    // GET RECOMMENDED POSTINGS
    // =====================================================

    @Operation(summary = "Get recommended postings for volunteer",
               description = "Returns filtered postings based on volunteer preferences, excluding already registered ones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Postings found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "No postings found")
    })
    @GetMapping("/recommend/{volunteerId}")
    public ResponseEntity<List<PostingResponse>> recommendPostings(
            @PathVariable Long volunteerId,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String domain,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletRequest request) {

        log.info("📥 Recommend postings request for volunteer {}: location={}, domain={}, date={}",
                volunteerId, location, domain, date);

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        List<PostingResponse> postings = matchingService.recommendPostings(
                volunteerId, location, domain, date, authHeader
        );

        if (postings.isEmpty()) {
            log.info(" No postings found for volunteer {}", volunteerId);
            return ResponseEntity.status(404).body(List.of());
        }

        log.info("Found {} postings for volunteer {}", postings.size(), volunteerId);
        return ResponseEntity.ok(postings);
    }

    // =====================================================
    // REGISTER VOLUNTEER FOR POSTING
    // =====================================================

    @Operation(summary = "Register volunteer for a posting",
               description = "Checks slots, reduces slot count, and updates both services")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully registered"),
        @ApiResponse(responseCode = "400", description = "Already registered or no slots available"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Volunteer or posting not found")
    })
    @PostMapping("/register/{volunteerId}/{postingId}")
    public ResponseEntity<Map<String, String>> registerForPosting(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId,
            HttpServletRequest request) {

        log.info("📥 Register request: volunteer {} for posting {}", volunteerId, postingId);

        // Extract JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // Extract userId from JWT and verify authorization
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // Only volunteer themselves or ADMIN can register
        if (!"ADMIN".equals(role) && !userId.equals(volunteerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden - You can only register yourself"));
        }

        String result = matchingService.registerVolunteerForPosting(volunteerId, postingId, authHeader);
        log.info("{}", result);

        return ResponseEntity.ok(Map.of(
                "message", result,
                "volunteerId", String.valueOf(volunteerId),
                "postingId", String.valueOf(postingId)
        ));
    }

    // =====================================================
    // UNREGISTER VOLUNTEER FROM POSTING
    // =====================================================

    @Operation(summary = "Unregister volunteer from a posting",
               description = "Increases slot count and updates both services")
    @DeleteMapping("/unregister/{volunteerId}/{postingId}")
    public ResponseEntity<Map<String, String>> unregisterFromPosting(
            @PathVariable Long volunteerId,
            @PathVariable Long postingId,
            HttpServletRequest request) {

        log.info("📥 Unregister request: volunteer {} from posting {}", volunteerId, postingId);

        // Extract JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // Extract userId from JWT and verify authorization
        Long userId = (Long) request.getAttribute("userId");
        String role = (String) request.getAttribute("role");

        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        // Only volunteer themselves or ADMIN can unregister
        if (!"ADMIN".equals(role) && !userId.equals(volunteerId)) {
            return ResponseEntity.status(403).body(Map.of("error", "Forbidden - You can only unregister yourself"));
        }

        String result = matchingService.unregisterVolunteerFromPosting(volunteerId, postingId, authHeader);
        log.info("{}", result);

        return ResponseEntity.ok(Map.of(
                "message", result,
                "volunteerId", String.valueOf(volunteerId),
                "postingId", String.valueOf(postingId)
        ));
    }
}
