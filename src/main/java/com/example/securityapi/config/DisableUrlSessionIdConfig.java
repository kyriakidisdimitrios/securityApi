package com.example.securityapi.config;

import jakarta.servlet.SessionTrackingMode;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.EnumSet;

@Configuration
public class DisableUrlSessionIdConfig {

    @Bean
    public ServletContextInitializer servletContextInitializer() {
        return servletContext -> {
            // ✅ Only use cookies for session tracking
            servletContext.setSessionTrackingModes(EnumSet.of(SessionTrackingMode.COOKIE));
        };
    }
}
