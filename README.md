# Matching Service
Intelligent Volunteer–Posting Recommendation and Registration Engine

## Overview
The Matching Service is a core component of the Volunteer Resource Management System (VRMS), responsible for intelligent recommendation and controlled volunteer–posting registration workflows. It integrates with the Posting Service and Volunteer Service to provide consistent, secure, and efficient matching between volunteer preferences and available NGO postings.
This microservice acts as the orchestration layer, ensuring valid recommendations, eligibility validation, and multi-service coordination for registration operations.

## VRMS Ecosystem Integration

The Matching Service operates within a larger distributed architecture containing:

1. User Service – Authentication, authorization, and JWT issuance

2. Volunteer Service – Volunteer profile storage and registration tracking

3. Posting Service – Management of NGO postings, slots, and posting metadata

4. Matching Service (this service) – Recommendation engine and controlled registration

5. Analytics Service – Reporting and usage insights

Each service communicates through REST interfaces and relies on JWT-based authentication.

## Key Features

### Recommendation Engine

Provides filtered postings based on domain, location, and date.

Excludes postings for which the volunteer is already registered.

Ensures only postings with available volunteer slots are returned.

Provides personalized recommendations for each volunteer.

### Volunteer–Posting Registration

Manages the full workflow of volunteer registration into postings.

Ensures idempotency: volunteers cannot register for the same posting twice.

Updates Posting Service slot count and Volunteer Service mappings atomically.

Implements rollback logic if any dependent service operation fails.

### Security and Authentication

Fully secured with JWT authentication.

Only the volunteer themselves or an ADMIN can register/unregister.

Extracts userId and role from JWT-enriched request attributes.

All endpoints require authenticated access.

### Cross-Service Coordination

Interacts with Volunteer Service and Posting Service using REST.

Maintains consistency using a two-phase pattern with rollback on failure.

Utilizes caching for efficient repeated fetches of posting data.

## API Endpoints
### 1. Get Recommended Postings

GET /api/v1/matching/recommend/{volunteerId}

Returns filtered postings based on optional location, domain, and date parameters.
Requires a valid JWT.
Automatically excludes already-registered postings.

### 2. Register Volunteer for a Posting

POST /api/v1/matching/register/{volunteerId}/{postingId}

Validates authorization, checks slot availability, updates Posting Service and Volunteer Service, and performs rollback if any step fails.
Accessible by ADMIN or the volunteer themselves.

### 3. Unregister Volunteer from a Posting

DELETE /api/v1/matching/unregister/{volunteerId}/{postingId}

Increases slot count and removes mapping in Volunteer Service.
Accessible by ADMIN or the volunteer themselves.

## Data Model

The Matching Service does not maintain its own database of postings. Instead, it consumes posting data in the following structure directly from the Posting Service:
 
{
  "id": "Long",
  
  "title": "String",
  
  "description": "String",
  
  "domain": "String",
  
  "location": "String",
  
  "pincode": "String",
  
  "volunteersNeeded": "Integer",
  
  "volunteersSpotLeft": "Integer",
  
  "startDate": "LocalDate",
  
  "endDate": "LocalDate",
  
  "ngoId": "Long",
  
  "volunteersRegistered": ["Long"]
  
}

## Getting Started

### Prerequisites

Java 17+

Maven 3.6+

Valid URL endpoints for Posting and Volunteer microservices

JWT authentication enabled at gateway or request filter level

### Installation

git clone <repository-url>
cd matching-service
mvn clean install

### Run Application

mvn spring-boot:run


### The service runs at:

http://localhost:<port>

### Swagger Documentation:

http://localhost:<port>/swagger-ui/index.html

### Configuration

Application Properties:

server.port=8081

spring.application.name=matching-service

posting.service.url=http://localhost:8082/api/v1/postings

volunteer.service.url=http://localhost:8083/api/v1/users/volunteers

spring.cache.type=simple


Accessible configuration parameters include service URLs, caching options, and server port settings.

## Architecture

### Technology Stack

Spring Boot 3

Spring Web

Spring Caching

RestTemplate inter-service communication

SpringDoc OpenAPI

JWT Authentication (validated upstream)

### Design Principles

Service-layer business logic encapsulation

DTO-based data handling

Integration with external services using REST

Consistency mechanisms with rollback support

Stateless, cache-enabled processing

## Security

JWT authentication is enforced on all endpoints.

Authorization is based on roles extracted from JWT (VOLUNTEER, ADMIN, etc.).

Access rules ensure volunteers can only act on their own behalf unless acting as ADMIN.

No sensitive data is stored within this service.

## Contributing

Fork the repository.

Create a feature branch.

Commit and push changes.

Open a pull request for review.

## Support

For assistance or reporting issues:

Submit an issue in the repository.

Refer to the Swagger documentation for endpoint usage.

Contact the project maintainers for inquiries.
