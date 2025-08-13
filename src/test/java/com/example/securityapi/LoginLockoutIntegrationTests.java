//// src/test/java/com/example/securityapi/LoginLockoutIntegrationTests.java
//package com.example.securityapi;
//
//import com.example.securityapi.model.Customer;
//import com.example.securityapi.repository.CustomerRepository;
//import com.example.securityapi.security.LoginAttemptService;
//import com.example.securityapi.utilities.CaptchaService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.context.TestConfiguration;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Primary;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.test.context.TestPropertySource;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.time.*;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//@TestPropertySource(properties = {
//        // Disable JPA bean validation in tests so we can persist the BCrypt hash
//        "spring.jpa.properties.javax.persistence.validation.mode=none",
//        // Match your lockout settings (minutes used for “baseline” test)
//        "security.auth.max-failed-attempts=10",
//        "security.auth.lockout-minutes=10"
//})
//class LoginLockoutIntegrationTests {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired CustomerRepository customerRepository;
//    @Autowired PasswordEncoder passwordEncoder;
//
//    // CAPTCHA always passes in tests
//    @MockBean CaptchaService captchaService;
//
//    @Autowired(required = false)
//    LoginAttemptService loginAttemptService;
//
//    // Inject our test clock (provided by TestConfig below)
//    @Autowired Clock testClock;
//
//    private static final String USERNAME = "lockout_user";
//    private static final String RAW_PASSWORD = "CorrectPass1!";
//    private static final String WRONG_PASSWORD = "NopeNope1!";
//    private static final int MAX_FAILS = 10;
//
//    @BeforeEach
//    void setup() {
//        customerRepository.findByUsername(USERNAME).ifPresent(customerRepository::delete);
//
//        Customer c = Customer.builder()
//                .username(USERNAME)
//                .password(passwordEncoder.encode(RAW_PASSWORD))
//                .name("John")
//                .surname("Doe")
//                .dateOfBirth(LocalDate.of(1990, 1, 1))
//                .address("123 Main St")
//                .phoneNumber("1234567890")
//                .email("john.doe@test.local")
//                .isAdmin(false)
//                .build();
//        customerRepository.save(c);
//
//        when(captchaService.validateCaptcha(any(), any())).thenReturn(true);
//
//        if (loginAttemptService != null) {
//            loginAttemptService.onSuccess(USERNAME); // clear counters
//        }
//    }
//
//    @Test
//    void lockoutAfterTooManyFailures_andStillLockedWithCorrectPassword() throws Exception {
//        // 1..(MAX_FAILS-1) -> normal error redirect
//        for (int i = 1; i < MAX_FAILS; i++) {
//            mockMvc.perform(post("/login")
//                            .secure(true)
//                            .param("username", USERNAME)
//                            .param("password", WRONG_PASSWORD)
//                            .param("captcha", "OK")
//                            .with(csrf()))
//                    .andExpect(status().is3xxRedirection())
//                    .andExpect(redirectedUrl("/login?error"));
//        }
//
//        // MAX_FAILS-th -> lockout
//        mockMvc.perform(post("/login")
//                        .secure(true)
//                        .param("username", USERNAME)
//                        .param("password", WRONG_PASSWORD)
//                        .param("captcha", "OK")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/login?locked&mins=10"));
//
//        // ✅ Still locked even with the RIGHT password
//        mockMvc.perform(post("/login")
//                        .secure(true)
//                        .param("username", USERNAME)
//                        .param("password", RAW_PASSWORD)
//                        .param("captcha", "OK")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/login?locked&mins=10"));
//    }
//
//    @Test
//    void unlocksAfterDuration_withoutSleeping() throws Exception {
//        // For this test we simulate a SHORT lockout window by advancing the injected clock.
//        // First, force a lock:
//        for (int i = 0; i < MAX_FAILS; i++) {
//            mockMvc.perform(post("/login")
//                            .secure(true)
//                            .param("username", USERNAME)
//                            .param("password", WRONG_PASSWORD)
//                            .param("captcha", "OK")
//                            .with(csrf()))
//                    .andExpect(status().is3xxRedirection());
//        }
//
//        // Confirm we are locked now
//        mockMvc.perform(post("/login")
//                        .secure(true)
//                        .param("username", USERNAME)
//                        .param("password", RAW_PASSWORD)
//                        .param("captcha", "OK")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrlPattern("/login?locked*"));
//
//        // ⏩ Fast-forward the clock by > lockout-minutes (10m here)
//        if (testClock instanceof MutableTestClock mtc) {
//            mtc.advance(Duration.ofMinutes(11));
//        }
//
//        // Now correct login should succeed and redirect to "/"
//        mockMvc.perform(post("/login")
//                        .secure(true)
//                        .param("username", USERNAME)
//                        .param("password", RAW_PASSWORD)
//                        .param("captcha", "OK")
//                        .with(csrf()))
//                .andExpect(status().is3xxRedirection())
//                .andExpect(redirectedUrl("/"));
//    }
//
//    // --- Test clock support (no sleeps needed) ---
//    @TestConfiguration
//    static class TestConfig {
//        @Bean
//        @Primary
//        public Clock testClock() {
//            return new MutableTestClock(Clock.systemUTC());
//        }
//    }
//
//    static class MutableTestClock extends Clock {
//        private final ZoneId zone;
//        private Instant now;
//
//        MutableTestClock(Clock base) {
//            this.zone = base.getZone();
//            this.now = Instant.now(base);
//        }
//
//        void advance(Duration d) {
//            now = now.plus(d);
//        }
//
//        @Override public ZoneId getZone() { return zone; }
//        @Override public Clock withZone(ZoneId zone) { return new MutableTestClock(Clock.fixed(now, zone)); }
//        @Override public Instant instant() { return now; }
//    }
//}
