package com.example.server.global.config;

import com.example.server.domain.news.dto.ClovaStudioProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(ClovaStudioProperties.class)
public class ClovaStudioConfig {
}