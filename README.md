# Matching-Service
Overview

The Matching Service connects volunteers with suitable postings by filtering based on location, domain, and date.
It works between the Volunteer and Posting services, secured fully using JWT authentication.

Recommendation Flow

The service fetches postings, removes already-registered ones, and applies user-selected filters.
JWT tokens ensure only authenticated volunteers can access personalized recommendations.

Registration & Authorization

Volunteers can register or unregister for postings, with slot updates handled across services.
JWT-based identity checks ensure only the volunteer or an admin can perform these actions.