package com.example.securityapi.config;

import jakarta.servlet.http.HttpSession;

public class ControllerUtil {
    //not used yet -- if (!ControllerUtil.isAdmin(session)) return "redirect:/login";
    public static boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }
}
