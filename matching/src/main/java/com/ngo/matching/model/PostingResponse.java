package com.ngo.matching.model;

import java.time.LocalDate;

public class PostingResponse {

    private Long postingId;
    private String ngoName;
    private String location;
    private LocalDate date;
    private int slotsAvailable;
    private boolean recommended;

    public PostingResponse() {
    }

    public PostingResponse(Long postingId, String ngoName, String location, LocalDate date, int slotsAvailable, boolean recommended) {
        this.postingId = postingId;
        this.ngoName = ngoName;
        this.location = location;
        this.date = date;
        this.slotsAvailable = slotsAvailable;
        this.recommended = recommended;
    }

    public Long getPostingId() { return postingId; }
    public String getNgoName() { return ngoName; }
    public String getLocation() { return location; }
    public LocalDate getDate() { return date; }
    public int getSlotsAvailable() { return slotsAvailable; }
    public boolean isRecommended() { return recommended; }

    public void setPostingId(Long postingId) { this.postingId = postingId; }
    public void setNgoName(String ngoName) { this.ngoName = ngoName; }
    public void setLocation(String location) { this.location = location; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setSlotsAvailable(int slotsAvailable) { this.slotsAvailable = slotsAvailable; }
    public void setRecommended(boolean recommended) { this.recommended = recommended; }
}
