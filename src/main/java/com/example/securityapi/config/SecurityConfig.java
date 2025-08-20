package com.example.securityapi.config;

import com.example.securityapi.security.*;
import com.example.securityapi.service.CustomerService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.PortMapperImpl;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import com.example.securityapi.security.MfaGateFilter;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableMethodSecurity() // Method-level checks → mitigates Missing Authorization (CWE-862) / Improper Authorization (CWE-285)
public class SecurityConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Value("${server.http.port:8080}")
    private String httpPort;

    @Value("${server.port:9443}")
    private String httpsPort;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Strong hashing for credentials → CWE-257/522
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    // Explicit lockout filter → mitigates brute-force (CWE-307)
    @Bean
    public LockoutFilter lockoutFilter(LoginAttemptService loginAttemptService) {
        return new LockoutFilter(loginAttemptService);
    }
    @Bean
    public MfaGateFilter mfaGateFilter(CustomerService customerService) {
        return new MfaGateFilter(customerService);
    }
    @Bean
    public MfaEnforcementFilter mfaEnforcementFilter(CustomerService customerService) {
        return new MfaEnforcementFilter(customerService);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           CaptchaValidationFilter captchaFilter,  // CAPTCHA throttles automation → CWE-307 (defense-in-depth)
                                           LockoutFilter lockoutFilter,            // CWE-307
                                           LoginSuccessHandler successHandler,     // generic messaging helps CWE-209/204
                                           LoginFailureHandler failureHandler,    // generic messaging helps CWE-209/204
                                           MfaEnforcementFilter mfaEnforcementFilter,
                                           MfaGateFilter mfaGateFilter)    // << here
            throws Exception {

        // HTTP→HTTPS mapping; redirects to TLS → mitigates Cleartext Transmission (CWE-319)
        PortMapperImpl portMapper = new PortMapperImpl();
        Map<String, String> mappings = new HashMap<>();
        mappings.put(httpPort, httpsPort);
        portMapper.setPortMappings(mappings);

        http
                // Enforce HTTPS everywhere → CWE-319
                .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                .portMapper(pm -> pm.portMapper(portMapper))

                // Security headers hardening
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(false)
                                .preload(false))               // HSTS → reduces downgrade/mixed-content risks (CWE-319)
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; script-src 'self'; style-src 'self' 'unsafe-inline'; frame-ancestors 'none'"))
                        // CSP limits script/inline/script-src → mitigates XSS (CWE-79/1336) & overall mech (CWE-693)
                        // frame-ancestors 'none' → Clickjacking (CWE-1021)
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        // Referrer-Policy reduces info leakage (CWE-200)
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        // Clickjacking defense (legacy) (CWE-1021)
                        .contentTypeOptions(cto -> {})      // X-Content-Type-Options → stop MIME sniffing (CWE-16 hardening)
                )

                // Session protection
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) // minimize surface (CWE-613 hardening)
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        // Fixation protection (CWE-384)
                        .invalidSessionUrl("/invalidSession")
                        .maximumSessions(1)                 // Concurrency limits; reduce hijack window (CWE-613)
                        .expiredUrl("/sessionExpired")
                )

                // CSRF: enabled by default for state-changing requests; Thymeleaf forms use tokens → CSRF (CWE-352)
                .csrf(csrf -> {})

                // Generic access denied page → avoid verbose errors (CWE-209/204)
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))

                // Authorization rules
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/register", "/captcha-image",
                                // ✅ This is the ONLY change: Moved session pages into the main permitAll list
                                "/invalidSession", "/sessionExpired", "/access-denied",
                                "/css/**", "/js/**", "/webjars/**", "/images/**", "/fonts/**",
                                "/ssrf-blocked",
                                "/mfa", "/mfa/**",                 // << allow MFA pages
                                "/error", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")  // Enforce admin boundaries → CWE-862/285
                        .requestMatchers("/customers/**").hasRole("ADMIN") // Protect PII list → CWE-359/200 + CWE-862/285
                        .requestMatchers("/account/security", "/account/mfa/**").authenticated()
                        .anyRequest().authenticated()
                )

                // Form login with custom handlers → neutral error messages (CWE-209/204)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())

                // Logout hygiene: allow GET + clear cookie/session → Session expiration (CWE-613)
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());

        // Filter order: Lockout → Captcha → Username/Password → limits brute force (CWE-307)
        http.addFilterBefore(lockoutFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(captchaFilter, LockoutFilter.class);
        http.addFilterAfter(mfaEnforcementFilter, UsernamePasswordAuthenticationFilter.class); // << add
        http.addFilterAfter(mfaGateFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}