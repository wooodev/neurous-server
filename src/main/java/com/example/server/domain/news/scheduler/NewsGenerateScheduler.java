package com.example.server.domain.news.scheduler;

import com.example.server.domain.news.service.ContentGenerationService;
import com.example.server.domain.news.service.NewsCrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class NewsGenerateScheduler {

    private final NewsCrawlingService newsCrawlingService;
    private final ContentGenerationService contentGenerationService;

//    @Scheduled(cron = "0 0 0/6 * * *", zone = "Asia/Seoul")
//    public void run() {
//
//        long startMs = System.currentTimeMillis();
//        OffsetDateTime windowStart = OffsetDateTime.now();
//
//        try{
//            log.info("[scheduler] start windowStart={}", windowStart);
//
//            int saved = newsCrawlingService.crawlAllCategories();
//            OffsetDateTime windowEnd = OffsetDateTime.now();
//            log.info("[scheduler] crawling done: saved={}, windowEnd={}", saved, windowEnd);
//
//            int generated = contentGenerationService.generateByCrawledWindow(windowStart, windowEnd);
//            log.info("[scheduler] generation done: generated={}", generated);
//
//            log.info("[scheduler] success elapsedMs={}", System.currentTimeMillis() - startMs);
//        } catch (Exception e) {
//            log.error("[scheduler] failed elapsedMs={}", System.currentTimeMillis() - startMs, e);
//        }
//
//    }
}