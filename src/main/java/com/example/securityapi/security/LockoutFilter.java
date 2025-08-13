package com.example.securityapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class LockoutFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LockoutFilter.class);
    private final LoginAttemptService attemptService;

    public LockoutFilter(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {

        if (isLoginPost(request)) {
            String username = request.getParameter("username");
            if (attemptService.isLocked(username)) {
                long mins = attemptService.minutesLeft(username);
                if (log.isDebugEnabled()) {
                    log.debug("Blocking login for '{}' — {} min left", username, mins);
                }
                response.sendRedirect("/login?locked&mins=" + mins);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private boolean isLoginPost(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod())
                && "/login".equals(request.getServletPath());
    }
}
