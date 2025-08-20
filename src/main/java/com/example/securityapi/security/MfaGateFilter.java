package com.example.securityapi.security;

import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class MfaGateFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(MfaGateFilter.class);
    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    private final CustomerService customerService;

    public MfaGateFilter(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * URLs that must NOT be intercepted by the MFA gate to avoid redirect loops.
     * (Includes: /account/mfa/** and /account/security)
     */
    private static final List<String> BYPASS = List.of(
            "/mfa", "/mfa/**",                // MFA UI + posts
            "/account/mfa/**",                // enable/disable/resend posts
            "/account/security",              // landing page after changes
            "/login", "/register",
            "/logout", "/customLogout",
            "/captcha-image",
            "/css/**", "/js/**", "/images/**", "/webjars/**", "/fonts/**",
            "/error", "/favicon.ico",
            "/invalidSession", "/sessionExpired", "/access-denied"
    );

    private static boolean matchesAny(String uri, List<String> patterns) {
        for (String p : patterns) {
            if (MATCHER.match(p, uri)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String uri = request.getRequestURI();
        final HttpSession session = request.getSession(false);

        // No session yet → nothing to enforce
        if (session == null) {
            chain.doFilter(request, response);
            return;
        }

        // Current MFA state from session
        final String pendingUser = (String) session.getAttribute("MFA_USERNAME");
        final boolean mfaVerified = Boolean.TRUE.equals(session.getAttribute("MFA_VERIFIED"));

        // 1) Always allow the BYPASS URLs through (prevents redirect loops and lets controllers run)
        if (matchesAny(uri, BYPASS)) {
            // Special case: if someone hits /mfa directly without a pending flow, send them to /login
            if ((MATCHER.match("/mfa", uri) || MATCHER.match("/mfa/**", uri)) && pendingUser == null) {
                log.debug("Accessed {} without pending MFA; redirecting to /login", uri);
                response.sendRedirect("/login");
                return;
            }
            chain.doFilter(request, response);
            return;
        }

        // 2) If session says "pending MFA" but database says "MFA disabled", clear stale flags
        if (pendingUser != null) {
            Customer c = customerService.findByUsername(pendingUser);
            if (c == null || !c.isMfaEnabled()) {
                log.debug("Clearing stale MFA state for user {}", pendingUser);
                session.removeAttribute("MFA_USERNAME");
                session.removeAttribute("MFA_VERIFIED");
                chain.doFilter(request, response);
                return;
            }
        }

        // 3) Normal gate: pending MFA and not verified → force /mfa
        if (pendingUser != null && !mfaVerified) {
            log.debug("MFA required for user {} on [{}] → redirect /mfa", pendingUser, uri);
            response.sendRedirect("/mfa");
            return;
        }

        chain.doFilter(request, response);
    }
}
