package com.example.securityapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

/*.csrf(csrf -> csrf.disable())  Disables CSRF (Cross-Site Request Forgery) protection.

        This is usually required if you're:

        Using POST requests from HTML forms without CSRF tokens

        Working with AJAX-based APIs or stateless sessions*/

/* .authorizeHttpRequests(auth -> auth
    .requestMatchers("/login", "/register", "/customLogout", "/css/**", "/js/**", "/webjars/**").permitAll()
    .anyRequest().permitAll())
    Grants public (unauthenticated) access to:
        Login, register, logout pages
        Static assets like CSS, JS, WebJars
        .anyRequest().permitAll() → allows everything else by default (even admin pages)
        This currently allows every route without authentication. */

@Configuration //Declares this class as a Spring config
@EnableWebSecurity //Enables Spring Security for your web app
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // needed for POST to work without tokens
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/customLogout", "/css/**", "/js/**", "/webjars/**")
                        .permitAll()
                        .anyRequest().permitAll() //Your current config is not enforcing login or roles. If you want to protect admin pages like /admin/**, you can later change to : .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                );

//        .permitAll()
//                .requestMatchers("/admin/**").authenticated() // 🔒 Admin pages require login
//                .anyRequest().authenticated() // 🔒 All other routes require login

        return http.build();
    }
}