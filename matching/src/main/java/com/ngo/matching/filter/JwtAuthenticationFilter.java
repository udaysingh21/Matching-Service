package com.ngo.matching.filter;

import com.ngo.matching.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getServletPath();

        // Skip authentication for public endpoints
        if (isPublicEndpoint(path)) {
            log.debug(" Public endpoint - skipping JWT validation: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("⚠️ No Authorization header or invalid format for: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            log.debug(" JWT token found, extracting claims...");

            String email = jwtService.extractUsername(jwt);
            String role = jwtService.extractRole(jwt);
            Long userId = jwtService.extractUserId(jwt);

            log.debug(" JWT Claims - Email: {}, Role: {}, UserId: {}", email, role, userId);

            // Set request attributes for controllers to use
            request.setAttribute("username", email);
            request.setAttribute("role", role);
            request.setAttribute("userId", userId);

            log.debug(" Request attributes set - userId: {}, role: {}", userId, role);

            // Set Spring Security authentication
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            log.debug(" SecurityContext authentication set for user: {}", email);

        } catch (Exception e) {
            log.error(" JWT processing failed: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private boolean isPublicEndpoint(String requestURI) {
        return requestURI.startsWith("/actuator") ||
               requestURI.startsWith("/swagger-ui") ||
               requestURI.startsWith("/v3/api-docs") ||
               requestURI.startsWith("/api-docs") ||
               requestURI.equals("/error");
    }
}
