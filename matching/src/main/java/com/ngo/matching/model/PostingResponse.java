package com.ngo.matching.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

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
    private LocalDate startDate;
    private LocalDate endDate;
    private Long ngoId;
    private String contactEmail;
    private String contactPhone;
    private String status;
    private String createdAt; // keep as String or use java.time
    private String updatedAt;
    private Integer volunteersSpotLeft;
    private List<Long> volunteersRegistered;
}
