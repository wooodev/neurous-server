package com.example.server.domain.news.service;

import com.example.server.domain.content.entity.Content;
import com.example.server.domain.content.entity.vo.ContentCategory;
import com.example.server.domain.content.entity.vo.ContentDifficulty;
import com.example.server.domain.content.entity.vo.ContentLevel;
import com.example.server.domain.content.repository.ContentRepository;
import com.example.server.domain.news.client.ClovaStudioClient;
import com.example.server.domain.news.dto.ClovaChatCompletionRequest;
import com.example.server.domain.news.dto.ClovaStudioProperties;
import com.example.server.domain.news.entity.NewsArticle;
import com.example.server.domain.news.entity.vo.NewsCategory;
import com.example.server.domain.news.repository.NewsArticleRepository;
import com.example.server.domain.quiz.service.QuizGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
@zProfile("!test")
public class ContentGenerationService {

    private final NewsArticleRepository newsArticleRepository;
    private final ContentRepository contentRepository;
    private final ClovaStudioClient clovaStudioClient;

    private final QuizGenerationService quizGenerationService;
    private final ArticleImageGenerationService articleImageGenerationService;

    private final ClovaStudioProperties props;

    @Value("${content.generation.per-category-size:10}")
    private int perCategorySize;

    @Value("${content.generation.max-retries:3}")
    private int maxRetries;

    /**
    * 수동 추출 메서드
    */
    @Transactional
    public int generateAllCategories() {
        int saved = 0;

        for (NewsCategory category : NewsCategory.values()) {
            List<NewsArticle> articles = newsArticleRepository
                    .findByCategoryOrderByPublishedAtDesc(category, PageRequest.of(0, perCategorySize))
                    .getContent();

            for (NewsArticle a : articles) {
                saved += generate3LevelsForOneArticle(a);
            }
        }

        return saved;
    }

    /**
     * 난이도 별 컨텐츠 데이터 생성
     */
    @Transactional
    public int generateByCrawledWindow(OffsetDateTime from, OffsetDateTime to) {
        int saved = 0;

        for (NewsCategory category : NewsCategory.values()) {
            List<NewsArticle> articles = newsArticleRepository
                    .findByCategoryAndCrawledAtBetweenOrderByCrawledAtAsc(category, from, to);

            for (NewsArticle a : articles) {
                saved += generate3LevelsForOneArticle(a);
            }
        }

        return saved;
    }

    private int generate3LevelsForOneArticle(NewsArticle a) {
        int saved = 0;

        saved += generateAndSave(a, "BEGINNER");
        saved += generateAndSave(a, "INTERMEDIATE");
        saved += generateAndSave(a, "ADVANCED");

        return saved;
    }


    private int generateAndSave(NewsArticle a, String diff) {

        ContentLevel level;
        try {
            level = ContentLevel.valueOf(diff); // "BEGINNER" 같은 문자열이어야 함
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("diff 값이 유효하지 않습니다: " + diff, e);
        }

        if (contentRepository.existsByNewsArticleIdAndContentLevel(a.getNewsArticleId(), level)) {
            return 0;
        }

        String prompt = buildPrompt(a, diff);

        String body = callClova(prompt, diff);

        String imageUrl = articleImageGenerationService.ensureImageUrl(a);

        Content entity = Content.builder()
                .title(a.getTitle())
                .content(body)
                .contentCategory(ContentCategory.valueOf(a.getCategory().name()))
                .contentLevel(ContentLevel.valueOf(diff))
                .imageUrl(imageUrl)
                .newsArticleId(a.getNewsArticleId())
                .batchTime(LocalDateTime.now())
                .contentDate(a.getPublishedAt().toLocalDateTime())
                .build();

        contentRepository.save(entity);

        Content saved = contentRepository.save(entity);

        try {
            quizGenerationService.generateQuizForContent(saved);
        } catch (Exception e) {
            log.error("[quiz] generation failed contentId={}", saved.getContentId(), e);
        }

        return 1;
    }

    private String callClova(String userPrompt, String diff) {
        String system = """
            당신은 뉴스 기반 학습 컨텐츠를 만드는 한국어 에디터입니다.
            
            분량/문단/문장 수 제약을 최우선으로 지키고, 그 범위 안에서 정보 밀도와 문장 구조를 조절해 읽기 시간을 맞추세요.
            
            각 문장은 반드시 마침표(.)로 끝내세요.
            문단 구분은 반드시 빈 줄(\\\\n\\\\n)로 하세요.
            
            정보 전달 목적의 학습용 읽기 자료입니다. 뉴스의 사실과 맥락을 이해하기 쉽게 풀어 설명하되, 의견·평가·의미 확장은 금지합니다.
            
            불릿/번호 목록 금지. 제목 출력 금지. &quot; 문자 제거. 컨텐츠 내용에 "제목:, 본문:" 이런 데이터가 안들어가게 하기.
            """;

        ClovaChatCompletionRequest req = new ClovaChatCompletionRequest(
                props.model(),
                List.of(
                        new ClovaChatCompletionRequest.Message("system", system),
                        new ClovaChatCompletionRequest.Message("user", userPrompt)
                ),
                0.8,
                maxTokensByDifficulty(diff)
        );

        return clovaStudioClient.chat(req);
    }

    private int maxTokensByDifficulty(String diff) {
        return switch (diff) {
            case "BEGINNER" -> 300;
            case "INTERMEDIATE" -> 700;
            case "ADVANCED" -> 1400;
            default -> 700;
        };
    }

    private String buildPrompt(NewsArticle a, String diff) {

        String source = firstNonBlank(a.getContentClean(), a.getDescription(), "");

        if (source.isBlank()) {
            throw new IllegalStateException("원문 데이터가 비었습니다. articleId=" + a.getNewsArticleId() + ", title=" + a.getTitle());
        }

        String constraint = switch (diff) {
            case "BEGINNER" -> """
                    [초급 제약]
                    - 분량: 150–250자
                    - 문단: 1개
                    - 문장: 3~5문장
                    - 읽기 시간: 약 1분
                    """;
            case "INTERMEDIATE" -> """
                    [중급 제약]
                    - 분량: 300–500자
                    - 문단: 2개 (문단은 빈 줄로 구분)
                    - 문장: 6~10문장
                    - 읽기 시간: 약 2분
                    """;
            case "ADVANCED" -> """
                    [고급 제약]
                    - 분량: 600–900자
                    - 문단: 3개 (문단은 빈 줄로 구분)
                    - 문장: 12~18문장
                    - 읽기 시간: 약 4분
                    """;
            default -> throw new IllegalArgumentException("잘못된 난이도: " + diff);
        };

        return """
                아래 뉴스 데이터로 학습용 요약/해설 컨텐츠를 작성하세요.
                카테고리: %s
                뉴스 제목: %s

                뉴스 본문(정리본):
                %s

                %s

                출력은 본문만 하세요.
                """.formatted(a.getCategory().name(), a.getTitle(), safeTrim(source, 2500), constraint);
    }

    private int countSentencesByDot(String t) {
        String[] parts = t.split("\\.");
        int count = 0;
        for (String p : parts) {
            if (!p.trim().isBlank()) count++;
        }
        return count;
    }

    private String safeTrim(String s, int max) {
        if (s == null) return "";
        String t = s.trim();
        return (t.length() <= max) ? t : t.substring(0, max);
    }

    private String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) return v;
        }
        return "";
    }

    private boolean isValid(String text, String diff) {
        if (text == null) return false;
        String t = text.trim();
        if (t.isBlank()) return false;

        int len = t.length();
        int paragraphs = t.split("\\n\\s*\\n").length;
        int sentences = countSentencesByDot(t);

        return switch (diff) {
            case "BEGINNER" -> (len >= 150 && len <= 250) && (paragraphs == 1) && (sentences >= 3 && sentences <= 5);
            case "INTERMEDIATE" -> (len >= 300 && len <= 500) && (paragraphs == 2) && (sentences >= 6 && sentences <= 10);
            case "ADVANCED" -> (len >= 600 && len <= 900) && (paragraphs == 3) && (sentences >= 12 && sentences <= 18);
            default -> false;
        };
    }

    private String preview(String s) {
        if (s == null) return "";
        String t = s.replace("\n", " ").trim();
        return (t.length() <= 80) ? t : t.substring(0, 80) + "...";
    }
}