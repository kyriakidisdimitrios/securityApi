package com.example.securityapi.config;
import jakarta.servlet.http.HttpSession;
//safer than session.getAttribute("isAdmin").equals(true). Static helper method to check if the currently logged-in user is an admin, based on their session attribute.
public class ControllerUtil {
    //not used yet -- if (!ControllerUtil.isAdmin(session)) return "redirect:/login";
    public static boolean isAdmin(HttpSession session) {
        return Boolean.TRUE.equals(session.getAttribute("isAdmin"));
    }
}
