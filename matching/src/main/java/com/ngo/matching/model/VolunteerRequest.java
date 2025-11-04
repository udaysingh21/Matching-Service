package com.ngo.matching.model;

import java.time.LocalDate;

public class VolunteerRequest {

    private Long volunteerId;
    private String location;
    private LocalDate availableFrom;
    private LocalDate availableTo;

    public VolunteerRequest() {}

    public VolunteerRequest(Long volunteerId, String location, LocalDate availableFrom, LocalDate availableTo) {
        this.volunteerId = volunteerId;
        this.location = location;
        this.availableFrom = availableFrom;
        this.availableTo = availableTo;
    }

    public Long getVolunteerId() { return volunteerId; }
    public String getLocation() { return location; }
    public LocalDate getAvailableFrom() { return availableFrom; }
    public LocalDate getAvailableTo() { return availableTo; }

    public void setVolunteerId(Long volunteerId) { this.volunteerId = volunteerId; }
    public void setLocation(String location) { this.location = location; }
    public void setAvailableFrom(LocalDate availableFrom) { this.availableFrom = availableFrom; }
    public void setAvailableTo(LocalDate availableTo) { this.availableTo = availableTo; }
}
