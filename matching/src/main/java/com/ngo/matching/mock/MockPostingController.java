package com.ngo.matching.mock;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/posting")
public class MockPostingController {

    private final Map<Long, Integer> postingSlots = new HashMap<>();

    public MockPostingController() {
        postingSlots.put(1L, 5); // postingId=1 has 5 slots (just for testing)
    }

    @GetMapping("/validate/{postId}")
    public boolean validate(@PathVariable Long postId) {
        return postingSlots.containsKey(postId) && postingSlots.get(postId) > 0;
    }

    @PostMapping("/assign/{postId}/{volId}")
    public String assign(@PathVariable Long postId, @PathVariable Long volId) {
        return "Saved in Posting MS";
    }

    @PostMapping("/reduceSlot/{postId}")
    public String reduceSlot(@PathVariable Long postId) {
        postingSlots.computeIfPresent(postId, (k, v) -> v - 1);
        return "Slot reduced";
    }
}
