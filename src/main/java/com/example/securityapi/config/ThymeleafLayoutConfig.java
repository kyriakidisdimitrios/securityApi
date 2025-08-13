package com.example.securityapi.config;


import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafLayoutConfig {
    @Bean //The @Bean method registers LayoutDialect in the application context
    public LayoutDialect layoutDialect() {

        return new LayoutDialect();
    }
}
