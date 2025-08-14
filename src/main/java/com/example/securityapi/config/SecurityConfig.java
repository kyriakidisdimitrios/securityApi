package com.example.securityapi.config;
import com.example.securityapi.security.CaptchaValidationFilter;
import com.example.securityapi.security.LockoutFilter;
import com.example.securityapi.security.LoginFailureHandler;
import com.example.securityapi.security.LoginSuccessHandler;
import com.example.securityapi.security.LoginAttemptService;
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
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
@Configuration
@EnableMethodSecurity() // keep method-level security
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
        return new BCryptPasswordEncoder(12);
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }
    // Keep LockoutFilter as an explicit bean (simple constructor)
    @Bean
    public LockoutFilter lockoutFilter(LoginAttemptService loginAttemptService) {
        return new LockoutFilter(loginAttemptService);
    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           // CaptchaValidationFilter is provided via @Component
                                           CaptchaValidationFilter captchaFilter,
                                           LockoutFilter lockoutFilter,
                                           LoginSuccessHandler successHandler,
                                           LoginFailureHandler failureHandler) throws Exception {
        // HTTP→HTTPS port mapping for redirects
        PortMapperImpl portMapper = new PortMapperImpl();
        Map<String, String> mappings = new HashMap<>();
        mappings.put(httpPort, httpsPort);
        portMapper.setPortMappings(mappings);
        http
                // Enforce HTTPS everywhere
                .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                .portMapper(pm -> pm.portMapper(portMapper))
                // Security headers (HSTS, CSP, Referrer-Policy, X-Frame-Options, X-Content-Type-Options)
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .maxAgeInSeconds(31536000)
                                .includeSubDomains(false)
                                .preload(false))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; img-src 'self' data:; script-src 'self'; style-src 'self' 'unsafe-inline'; frame-ancestors 'none'"))
                        .referrerPolicy(rp -> rp.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.SAME_ORIGIN))
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                        .contentTypeOptions(cto -> {})
                )
                // Session protection
                .sessionManagement(sess -> sess
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        .invalidSessionUrl("/invalidSession")
                        .maximumSessions(1)
                        .expiredUrl("/sessionExpired"))
                // CSRF is handled by Spring Security form login; explicit config left empty
                .csrf(csrf -> {})
                // Access-denied page
                .exceptionHandling(ex -> ex.accessDeniedPage("/access-denied"))
                // Authorization rules (KEEP existing plus add /customers/** as admin-only)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/login", "/register", "/captcha-image",
                                "/invalidSession", "/sessionExpired", "/access-denied",
                                "/css/**", "/js/**", "/webjars/**", "/images/**", "/fonts/**",
                                "/ssrf-blocked",
                                "/error", "/favicon.ico"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/customers/**").hasRole("ADMIN") // <-- NEW: protect customer list/PII
                        .anyRequest().authenticated()
                )
                // Form login (custom success/failure handlers + CAPTCHA filter)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(successHandler)
                        .failureHandler(failureHandler)
                        .permitAll())
                // Logout
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll());
        // Filters: Lockout → Captcha → UsernamePasswordAuthenticationFilter
        http.addFilterBefore(lockoutFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterAfter(captchaFilter, LockoutFilter.class);
        return http.build();
    }
}