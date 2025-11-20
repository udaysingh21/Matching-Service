package com.ngo.matching.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
public class PostingResponse {

    private Long id;
    private String title;
    private String description;
    private String domain;
    private String location;

    private String city;
    private String state;
    private String country;
    private String pincode;

    private String effortRequired;

    private Integer volunteersNeeded;
    private Integer volunteersSpotLeft;

    private LocalDate startDate;
    private LocalDate endDate;

    private Long ngoId;

    private String contactEmail;
    private String contactPhone;

    private String status; // Enum from Posting Service → treat it as String here

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Set<Long> volunteersRegistered;
}
