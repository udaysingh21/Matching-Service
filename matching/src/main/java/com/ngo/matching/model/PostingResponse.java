package com.ngo.matching.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class PostingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // comment later since id comes from posting service
    private Long id;

    private String title;
    private String description;
    private String domain;

    private String pincode;     // NEW
    private String city;
    private String state;
    private String country;

    private Integer volunteersNeeded;  // replaces slotsAvailable

    private LocalDate startDate;
    private LocalDate endDate;

    private Long ngoId;
    private String contactEmail;
    private String contactPhone;

    private String status;

    private LocalDate createdAt;
    private LocalDate updatedAt;
}
