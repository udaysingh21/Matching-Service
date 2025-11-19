package com.ngo.matching.mock;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/volunteer")
public class MockVolunteerController {

    // Fake storage: volunteerId → list of applied postings
    private final Map<Long, Set<Long>> appliedMap = new HashMap<>();

    @GetMapping("/hasApplied/{volId}/{postId}")
    public boolean hasApplied(@PathVariable Long volId, @PathVariable Long postId) {
        return appliedMap.getOrDefault(volId, new HashSet<>()).contains(postId);
    }

    @PostMapping("/apply/{volId}/{postId}")
    public String apply(@PathVariable Long volId, @PathVariable Long postId) {
        appliedMap.computeIfAbsent(volId, k -> new HashSet<>()).add(postId);
        return "Saved in Volunteer MS";
    }
}
