package com.example.securityapi.controller;

import com.example.securityapi.model.User;
import com.example.securityapi.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Home page
    @GetMapping
    public String home() {
        return "index";
    }

    // User list page
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
        // TODO: Restrict this page to ADMIN only in the future with Spring Security
    }

    // Registration form display
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User()); // binds form fields to User.name, email, password
        return "register";
    }

    // Registration form submission
    @PostMapping("/register")
    public String registerUser(@ModelAttribute("user") User user) {
        userService.saveUser(user); // stores name, email, and password
        return "redirect:/login";
    }

    // Login page (not yet functional)
    @GetMapping("/login")
    public String showLoginForm() {
        return "login"; // static login page
        // TODO: Integrate Spring Security login logic here
    }
    @PostMapping("/login")
    public String loginUser(@RequestParam String name,
                            @RequestParam String password,
                            Model model) {
        boolean authenticated = userService.authenticateUser(name, password);

        if (authenticated) {
            return "redirect:/"; // login successful, go to home
        } else {
            model.addAttribute("error", "Invalid username or password");
            return "login"; // show login page with error
        }
    }


}
// Note: The login functionality is not yet implemented. This controller currently serves static pages.