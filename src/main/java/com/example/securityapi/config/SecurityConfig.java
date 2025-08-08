package com.example.securityapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.PortMapperImpl;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Value("${server.http.port:8080}")
    private String httpPort;

    @Value("${server.port:9443}")
    private String httpsPort;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Map HTTP→HTTPS so Spring Security knows how to build the redirect
        PortMapperImpl portMapper = new PortMapperImpl();
        Map<String, String> mappings = new HashMap<>();
        mappings.put(httpPort, httpsPort);     // e.g., 8080 -> 9443
        portMapper.setPortMappings(mappings);

        http
                .csrf(csrf -> csrf.disable())
                // 🔒 Force HTTPS for every request (this is what triggers the redirect)
                .requiresChannel(ch -> ch.anyRequest().requiresSecure())
                .portMapper(pm -> pm.portMapper(portMapper))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register", "/customLogout", "/css/**", "/js/**", "/webjars/**").permitAll()
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
