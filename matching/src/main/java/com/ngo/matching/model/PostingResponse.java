package com.ngo.matching.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "postings")
public class PostingResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postingId;

    private String ngoName;
    private String location;
    private LocalDate date;
    private int slotsAvailable;
    private boolean recommended;

    public PostingResponse() {
        // default constructor
    }

    public PostingResponse(Long postingId, String ngoName, String location, LocalDate date,
                           int slotsAvailable, boolean recommended) {
        this.postingId = postingId;
        this.ngoName = ngoName;
        this.location = location;
        this.date = date;
        this.slotsAvailable = slotsAvailable;
        this.recommended = recommended;
    }

    public Long getPostingId() {
        return postingId;
    }

    public void setPostingId(Long postingId) {
        this.postingId = postingId;
    }

    public String getNgoName() {
        return ngoName;
    }

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

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }
}
