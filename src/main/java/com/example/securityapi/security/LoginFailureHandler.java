package com.example.securityapi.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import static com.example.securityapi.utilities.LogSanitizer.s;

import java.io.IOException;

@Component
public class LoginFailureHandler implements AuthenticationFailureHandler {
    private static final Logger log = LoggerFactory.getLogger(LoginFailureHandler.class);

    // 👈 use the singular type
    private final LoginAttemptService attemptService;

    public LoginFailureHandler(LoginAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        attemptService.onFailure(username);

        if (attemptService.isLocked(username)) {
            long mins = attemptService.minutesLeft(username);
            log.warn("Account temporarily locked for user='{}' ({} min left)", s(username), mins);
            response.sendRedirect("/login?locked" + (mins > 0 ? ("&mins=" + mins) : ""));
        } else {
            log.warn("Authentication failed for user='{}'", s(username));
            response.sendRedirect("/login?error");
        }
    }
}
