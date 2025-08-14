package com.example.securityapi.security;
import com.example.securityapi.utilities.CaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
@Component
public class CaptchaValidationFilter extends OncePerRequestFilter {
    private final CaptchaService captchaService;
    // ✅ Constructor injection — Spring will inject the bean
    public CaptchaValidationFilter(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        if ("POST".equalsIgnoreCase(request.getMethod()) && "/login".equals(request.getServletPath())) {
            HttpSession session = request.getSession(false);
            String captcha = request.getParameter("captcha");
            if (session == null || captchaService.validateCaptchaCustom(captcha, session)) {
                response.sendRedirect("/login?error=Invalid%20CAPTCHA");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}