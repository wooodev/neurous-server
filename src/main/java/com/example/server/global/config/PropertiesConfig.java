package com.example.server.global.config;

import com.example.server.domain.news.dto.NewsCrawlingProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NewsCrawlingProperties.class)
public class PropertiesConfig {
}