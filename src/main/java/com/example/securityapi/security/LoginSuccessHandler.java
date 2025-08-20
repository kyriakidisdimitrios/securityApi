package com.example.securityapi.security;

import com.example.securityapi.model.Customer;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
    private final CustomerService customerService;
    private final LoginAttemptService attemptService;
    private final MfaService mfaService;

    public LoginSuccessHandler(CustomerService customerService,
                               LoginAttemptService attemptService,
                               MfaService mfaService) {
        this.customerService = customerService;
        this.attemptService = attemptService;
        this.mfaService = mfaService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        HttpSession session = request.getSession();
        String username = authentication.getName();
        Customer c = customerService.findByUsername(username);

        // ✅ Always clear failed-attempt counters on successful password auth
        attemptService.onSuccess(username);

        if (c != null && c.isMfaEnabled()) {
            // 🔐 Step-up MFA: do NOT mark user as logged in yet
            session.setAttribute("MFA_USERNAME", username);
            session.setAttribute("MFA_VERIFIED", Boolean.FALSE);

            // Generate + email OTP and persist hash/expiry
            mfaService.initiateMfa(c);

            // Important: /mfa must be permitAll in SecurityConfig
            response.sendRedirect("/mfa");
            return;
        }

        // ✅ No MFA required → normal session attributes
        session.setAttribute("loggedInUser", username);
        session.setAttribute("isAdmin", c != null && c.isAdmin());

        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        response.sendRedirect(isAdmin ? "/admin/books" : "/");
    }
}
