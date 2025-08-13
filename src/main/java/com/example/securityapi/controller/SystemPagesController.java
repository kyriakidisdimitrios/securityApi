package com.example.securityapi.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@Controller
public class SystemPagesController {

    @GetMapping("/sessionExpired")
    public String sessionExpired() {
        return "sessionExpired";
    }

    @GetMapping("/invalidSession")
    public String invalidSession() {
        return "invalidSession";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access_denied";
    }
    // ✅ NEW: SSRF-blocked landing page
    @GetMapping("/ssrf-blocked")
    public String ssrfBlocked(
            @RequestParam(value = "reason", required = false, defaultValue = "Unsafe or invalid URL") String reason,
            @RequestParam(value = "url", required = false) String url,
            Model model) {
        model.addAttribute("reason", reason);
        model.addAttribute("url", url);
        return "ssrf_blocked"; // renders templates/ssrf_blocked.html
    }
}
