package com.example.securityapi.controller;

import com.example.securityapi.model.Customer;
import com.example.securityapi.security.LoginAttemptService;
import com.example.securityapi.security.MfaService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mfa")
public class MfaController {
    private final CustomerService customerService;
    private final MfaService mfaService;
    private final LoginAttemptService attemptService;

    public MfaController(CustomerService customerService,
                         MfaService mfaService,
                         LoginAttemptService attemptService) {
        this.customerService = customerService;
        this.mfaService = mfaService;
        this.attemptService = attemptService;
    }

    @GetMapping
    public String mfaPage(HttpSession session, Model model) {
        String username = (String) session.getAttribute("MFA_USERNAME");
        if (username == null) return "redirect:/login";
        model.addAttribute("username", username);
        return "mfa_verify";
    }
    @PostMapping("/cancel")
    public String cancel(HttpSession session,
                         HttpServletResponse response) {
        // drop MFA state
        session.removeAttribute("MFA_USERNAME");
        session.removeAttribute("MFA_VERIFIED");

        // end the session completely
        try { session.invalidate(); } catch (IllegalStateException ignored) {}

        // also clear JSESSIONID cookie (like your customLogout)
        ResponseCookie cookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return "redirect:/login?mfaCanceled";
    }
    @PostMapping("/verify")
    public String verify(@RequestParam("code") String code,
                         HttpSession session,
                         RedirectAttributes ra) {
        String username = (String) session.getAttribute("MFA_USERNAME");
        if (username == null) return "redirect:/login";

        Customer c = customerService.findByUsername(username);
        if (c == null || !c.isMfaEnabled()) return "redirect:/login";

        if (mfaService.verify(c, code)) {
            // Success → mark verified and complete login session attrs
            session.setAttribute("MFA_VERIFIED", Boolean.TRUE);
            session.removeAttribute("MFA_USERNAME");
            session.setAttribute("loggedInUser", c.getUsername());
            session.setAttribute("isAdmin", c.isAdmin());
            attemptService.onSuccess(c.getUsername()); // clear counters after full auth
            return "redirect:" + (c.isAdmin() ? "/admin/books" : "/");
        }

        ra.addFlashAttribute("error", "Invalid or expired code. Please try again.");
        return "redirect:/mfa";
    }

    @PostMapping("/resend")
    public String resend(HttpSession session, RedirectAttributes ra) {
        String username = (String) session.getAttribute("MFA_USERNAME");
        if (username == null) return "redirect:/login";
        Customer c = customerService.findByUsername(username);
        if (c == null) return "redirect:/login";

        mfaService.resend(c);
        ra.addFlashAttribute("info", "A new verification code was sent to your email.");
        return "redirect:/mfa";
    }
}
