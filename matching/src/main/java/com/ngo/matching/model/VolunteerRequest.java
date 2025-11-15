package com.ngo.matching.model;

import java.time.LocalDate;

public class VolunteerRequest {

    private String location;      // optional
    private LocalDate date;       // optional

    public VolunteerRequest() {}

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
}
