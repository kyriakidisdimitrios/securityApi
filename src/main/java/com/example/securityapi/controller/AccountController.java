package com.example.securityapi.controller;

import com.example.securityapi.model.Customer;
import com.example.securityapi.security.MfaService;
import com.example.securityapi.service.CustomerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final CustomerService customerService;
    private final MfaService mfaService;

    public AccountController(CustomerService customerService, MfaService mfaService) {
        this.customerService = customerService;
        this.mfaService = mfaService;
    }

    @GetMapping("/security")
    public String securityPage(HttpSession session, Model model,
                               @ModelAttribute("successMessage") String success,
                               @ModelAttribute("errorMessage") String error) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer c = customerService.findByUsername(username);
        if (c == null) return "redirect:/login";

        model.addAttribute("mfaEnabled", c.isMfaEnabled());
        model.addAttribute("email", c.getEmail());
        return "account_security";
    }

    @PostMapping("/mfa/enable")
    public String enableMfa(HttpSession session, RedirectAttributes ra) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer c = customerService.findByUsername(username);
        if (c == null) return "redirect:/login";

        if (c.getEmail() == null || c.getEmail().isBlank()) {
            ra.addFlashAttribute("errorMessage", "Cannot enable MFA: your account has no email.");
            return "redirect:/account/security";
        }

        // ✅ Toggle MFA without touching password validation paths
        customerService.updateMfaEnabledByUsername(username, true);

        // Optionally prime a code so the user immediately receives an email.
        // initiateMfa(...) should persist code hash & expiry internally (or via customerService.persistMfaState).
        c = customerService.findByUsername(username); // refresh entity
        mfaService.initiateMfa(c);

        ra.addFlashAttribute("successMessage", "MFA enabled. We’ve sent a verification code to your email.");
        return "redirect:/account/security";
    }

    @PostMapping("/mfa/disable")
    public String disableMfa(HttpSession session, RedirectAttributes ra) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer c = customerService.findByUsername(username);
        if (c == null) return "redirect:/login";

        // ✅ Turn MFA off and clear code fields without triggering password checks
        customerService.updateMfaEnabledByUsername(username, false);

        // Clear any pending MFA state in the current session
        session.removeAttribute("MFA_USERNAME");
        session.removeAttribute("MFA_VERIFIED");

        ra.addFlashAttribute("successMessage", "MFA disabled for your account.");
        return "redirect:/account/security";
    }

    @PostMapping("/mfa/resend")
    public String resend(HttpSession session, RedirectAttributes ra) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) return "redirect:/login";

        Customer c = customerService.findByUsername(username);
        if (c == null) return "redirect:/login";

        mfaService.resend(c); // keep your service-level throttling
        ra.addFlashAttribute("successMessage", "A new verification code was sent to your email.");
        return "redirect:/account/security";
    }
}
