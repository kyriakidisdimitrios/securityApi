package com.example.securityapi.security;

import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class MfaEnforcementFilter extends OncePerRequestFilter {

    private final CustomerService customerService;

    public MfaEnforcementFilter(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (session != null && auth != null && auth.isAuthenticated()) {
            String username = auth.getName();

            Boolean mfaVerified = (Boolean) session.getAttribute("MFA_VERIFIED");
            String pending = (String) session.getAttribute("MFA_USERNAME");

            // Only arm MFA if user needs it and not already verified or pending
            if ((mfaVerified == null || !mfaVerified) && pending == null) {
                Customer c = customerService.findByUsername(username);
                if (c != null && c.isMfaEnabled()) {
                    session.setAttribute("MFA_USERNAME", username);
                    session.setAttribute("MFA_VERIFIED", Boolean.FALSE);
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
