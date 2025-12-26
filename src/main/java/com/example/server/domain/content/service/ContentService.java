package com.example.server.domain.content.service;

import com.example.server.domain.content.dto.ContentResponse;
import com.example.server.domain.content.dto.ContentDifficultyRequest;
import com.example.server.domain.content.dto.DifficultyRecommendResponse;
import com.example.server.domain.content.entity.Content;
import com.example.server.domain.content.entity.ContentDifficultyEvaluation;
import com.example.server.domain.content.entity.DifficultyBasetime;
import com.example.server.domain.content.entity.ReadContent;
import com.example.server.domain.content.entity.vo.ContentDifficulty;
import com.example.server.domain.content.entity.vo.DifficultyRecommend;
import com.example.server.domain.content.repository.*;
import com.example.server.domain.user.entity.vo.UserField;
import com.example.server.domain.user.repository.UserRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.BadRequestException;
import com.example.server.global.exception.model.ConflictException;
import com.example.server.global.exception.model.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ContentService {

    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final UserInterestRepository userInterestRepository;
    private final ReadContentRepository readContentRepository;
    private final ContentDifficultyEvaluationRepository contentDifficultyEvaluationRepository;
    private final DifficultyBasetimeRepository difficultyBasetimeRepository;

    private static final List<String> CATEGORIES = List.of("정치", "경제", "사회", "생활/문화", "IT/과학", "세계");
    private static final int RESULT_SIZE = 3;

    /**
     * 탐색 페이지 컨텐츠 조회
     */
    public Map<String, List<ContentResponse>> getExploreContent(Long userId, int page){
        int size = 10;
        PageRequest pageRequest = PageRequest.of(page, size);

        String ContentDiff = userRepository.findLevelByUserId(userId)
                .orElse("초급");

        Map<String, List<ContentResponse>> result = new LinkedHashMap<>();

        for(String category : CATEGORIES){
            List<Content> contents = contentRepository.findByContentDiffAndContentCategory(ContentDiff, category, pageRequest);

            result.put(category, contents.stream().map(ContentResponse::from).toList());
        }

        return result;
    }

    /**
     * 오늘의 미션 컨텐츠 조회
     */
    public List<ContentResponse> getTodayContent(Long userId) {
        String difficulty = userRepository.findLevelByUserId(userId).orElse("초급");

        List<String> categories = userInterestRepository.findInterestsByUserIdOrderByPriorityAsc(userId).stream()
                .map(UserField::getDescription)
                .toList();

        if (categories.isEmpty()) {
            throw new BadRequestException(ErrorMessage.CONTENT_INTEREST_NOT_SET);
        }

        double[] weights = normalizeWeights(categories.size());

        Random random = new Random();
        List<ContentResponse> result = new ArrayList<>();
        List<Integer> excludedIds = new ArrayList<>();

        for (int i = 0; i < RESULT_SIZE; i++) {
            String pickedCategory = pickCategoryByWeight(categories, weights, random);

            Content picked = pickOneContent(userId, difficulty, pickedCategory, excludedIds);

            if (picked == null) {
                throw new NotFoundException(ErrorMessage.CONTENT_TODAY_NOT_AVAILABLE);
            }

            excludedIds.add(picked.getContentId());
            result.add(ContentResponse.from(picked));
        }

        return result;
    }

    private double[] normalizeWeights(int categoryCount) {
        int n = Math.min(categoryCount, 3);

        if (n == 1) {
            return new double[]{1.0};
        }
        if (n == 2) {
            return new double[]{0.6, 0.4};
        }
        // n == 3
        return new double[]{0.5, 0.3, 0.2};
    }

    private String pickCategoryByWeight(List<String> categories, double[] weights, Random random) {
        int n = categories.size();
        double r = random.nextDouble();
        double acc = 0.0;

        for (int i = 0; i < n; i++) {
            acc += weights[i];
            if (r < acc) return categories.get(i);
        }
        return categories.get(0);
    }

    private Content pickOneContent(Long userId, String difficulty, String category, List<Integer> excludedIds) {
        if (excludedIds == null || excludedIds.isEmpty()) {
            return contentRepository.findRandomUnreadByContentDiffAndCategory(userId, difficulty, category).orElse(null);
        }
        return contentRepository.findRandomUnreadByContentDiffAndCategoryExcludeIds(userId, difficulty, category, excludedIds).orElse(null);
    }

    /**
     * 컨텐츠 상세 정보 조회
     */
    public ContentResponse getContentDetail(int contentId){
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CONTENT_NOT_FOUND));

        return ContentResponse.from(content);
    }

    /**
     * 검색 기능(title 기반)
     */
    public List<ContentResponse> search(Long userId, String keyword, int page) {

        String k = (keyword == null) ? "" : keyword.trim();
        if (k.isEmpty()) {
            return List.of();
        }

        int size = 10;

        String diff = userRepository.findLevelByUserId(userId).orElse("초급");

        return contentRepository.searchByTitle(
                        diff,
                        k,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(ContentResponse::from)
                .toList();
    }

    /**
     * 읽은 내역 조회
     */
    public List<ContentResponse> getReadHistory(Long userId, int page) {

        int size = 10;

        List<Content> contents = readContentRepository.findReadContentsByUserId(
                userId,
                PageRequest.of(page, size)
        );

        return contents.stream()
                .map(ContentResponse::from)
                .toList();
    }

    /**
     * 문제 난이도 평가
     */
    public DifficultyRecommendResponse setDifficultyEvaluation(Long userId, int contentId, ContentDifficultyRequest difficulty){

        if (contentDifficultyEvaluationRepository.findByUserIdAndContentId(userId, contentId).isPresent()) {
            throw new ConflictException(ErrorMessage.CONTENT_ALREADY_EVALUATED);
        }

        ContentDifficultyEvaluation c = ContentDifficultyEvaluation.builder()
                .contentId(contentId)
                .userId(userId)
                .contentDifficulty(difficulty.difficulty())
                .createdAt(LocalDateTime.now())
                .build();

        contentDifficultyEvaluationRepository.save(c);

        // 난이도 추천 로직
        DifficultyBasetime basetime = difficultyBasetimeRepository.findTopByUserIdOrderByBaseTimeDesc(userId)
                .orElseGet(() -> difficultyBasetimeRepository.save(DifficultyBasetime.now(userId)));

        LocalDateTime from = basetime.getBaseTime();
        LocalDateTime to = LocalDateTime.now();

        long evaluationEASYCount = contentDifficultyEvaluationRepository
                .countByUserIdAndDifficultyBetween(userId, ContentDifficulty.EASY, from, to);

        long evaluationHARDCount = contentDifficultyEvaluationRepository
                .countByUserIdAndDifficultyBetween(userId, ContentDifficulty.HARD, from, to);

        DifficultyRecommend recommend = DifficultyRecommend.NONE;

        if (evaluationEASYCount >= 13) {
            recommend = DifficultyRecommend.INCREASE;
        } else if (evaluationHARDCount >= 8) {
            recommend = DifficultyRecommend.DECREASE;
        }

        LocalDateTime now = LocalDateTime.now();

        if (recommend != DifficultyRecommend.NONE) {
            basetime.reset(now);
        }

        DifficultyRecommendResponse response = new DifficultyRecommendResponse(recommend);

        return response;
    }

    /**
     * 읽음 체크
     */
    public void setContentRead(Long userId, int contentId) {

        if (!contentRepository.existsById(contentId)) {
            throw new NotFoundException(ErrorMessage.CONTENT_NOT_FOUND);
        }

        if (readContentRepository.findByUserIdAndContentId(userId, contentId).isPresent()) {
            throw new ConflictException(ErrorMessage.CONTENT_ALREADY_READ);
        }

        ReadContent readContent = ReadContent.of(userId, contentId, LocalDateTime.now());

        readContentRepository.save(readContent);
    }

}
