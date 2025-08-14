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
    private final LoginAttemptService attemptService; // ✅ new
    public LoginSuccessHandler(CustomerService customerService,
                               LoginAttemptService attemptService) {
        this.customerService = customerService;
        this.attemptService = attemptService; // ✅
    }
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        // New session (session fixation protection is enabled in SecurityConfig)
        HttpSession session = request.getSession();
        String username = authentication.getName();
        Customer c = customerService.findByUsername(username);
        // Preserve your existing behavior so other code remains untouched
        session.setAttribute("loggedInUser", username);
        session.setAttribute("isAdmin", c != null && c.isAdmin());
        // ✅ clear failed-attempt counters on success
        attemptService.onSuccess(username);
        boolean isAdmin = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        response.sendRedirect(isAdmin ? "/admin/books" : "/");
    }
}