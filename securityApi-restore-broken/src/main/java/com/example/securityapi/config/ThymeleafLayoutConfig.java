package com.example.securityapi.config;


import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThymeleafLayoutConfig {
//<html xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout" layout:decorate="~{layout}">
//Without registering LayoutDialect, this line would fail silently or throw a template exception.

    @Bean //The @Bean method registers LayoutDialect in the application context
    public LayoutDialect layoutDialect() {

        return new LayoutDialect();
    }
}
