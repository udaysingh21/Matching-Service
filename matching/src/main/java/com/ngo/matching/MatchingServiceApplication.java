package com.ngo.matching;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class MatchingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MatchingServiceApplication.class, args);
    }

    // ✅ RestTemplate Bean (Required for calling Volunteer MS & Posting MS)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
