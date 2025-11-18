package com.ngo.matching.model;

public class VolunteerRequest {

    private String location; // optional
    private String domain;    // optional
    private String date;     // optional (ISO yyyy-MM-dd)

    public VolunteerRequest() {}

    public VolunteerRequest(String location, String domain, String date) {
        this.location = location;
        this.domain = domain;
        this.date = date;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
