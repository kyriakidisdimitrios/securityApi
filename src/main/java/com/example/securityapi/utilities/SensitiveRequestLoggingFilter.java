package com.example.securityapi.utilities;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Logs requests WITHOUT leaking sensitive data.
 * - Masks password / card fields in query/form logs (CWE-311 support).
 * - Skips static assets to avoid noise.
 * NOTE: Does not consume request bodies (JSON, etc.).
 */
@Component
@Order(5) // early, but after core servlet filters; keep ahead of app controllers
public class SensitiveRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(SensitiveRequestLoggingFilter.class);

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String p = request.getServletPath();
        return p.startsWith("/css/")
                || p.startsWith("/js/")
                || p.startsWith("/images/")
                || p.startsWith("/webjars/")
                || p.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        if (log.isDebugEnabled()) {
            try {
                Map<String, String[]> safeParams =
                        SensitiveDataSanitizer.maskParams(request.getParameterMap());
                // Only method + path (avoid raw query string leakage)
                log.debug("REQ {} {} params={}", request.getMethod(), request.getRequestURI(), safeParams);
            } catch (Exception ignore) {
                // Never break the request because of logging
            }
        }

        chain.doFilter(request, response);
    }
}
