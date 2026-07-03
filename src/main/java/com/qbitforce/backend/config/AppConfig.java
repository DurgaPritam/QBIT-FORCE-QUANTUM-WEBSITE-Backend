package com.qbitforce.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    JwtProperties.class,
    CorsProperties.class,
    ContactProperties.class,
    AdminProperties.class,
    AppUrlProperties.class,
    CloudinaryProperties.class
})
public class AppConfig {}
