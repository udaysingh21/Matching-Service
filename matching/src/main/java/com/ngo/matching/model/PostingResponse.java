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

    private String ngoName;
    private String location;
    private LocalDate date;
    private int slotsAvailable;
    private String domain;

    public PostingResponse() {}

    public PostingResponse(String ngoName, String location, LocalDate date, int slotsAvailable, String domain) {
        this.ngoName = ngoName;
        this.location = location;
        this.date = date;
        this.slotsAvailable = slotsAvailable;
        this.domain = domain;
    }

    private LocalDate startDate;
    private LocalDate endDate;

    private Long ngoId;
    private String contactEmail;
    private String contactPhone;

    private String status;

    public void setNgoName(String ngoName) {
        this.ngoName = ngoName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getSlotsAvailable() {
        return slotsAvailable;
    }

    public void setSlotsAvailable(int slotsAvailable) {
        this.slotsAvailable = slotsAvailable;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
