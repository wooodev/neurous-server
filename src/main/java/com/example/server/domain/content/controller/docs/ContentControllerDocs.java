package com.example.server.domain.content.controller.docs;

import com.example.server.domain.content.dto.ContentDifficultyRequest;
import com.example.server.domain.content.dto.ContentResponse;
import com.example.server.domain.content.dto.DifficultyRecommendResponse;

import com.example.server.domain.quiz.dto.ReadContentDetailResponse;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@Tag(name = "[콘텐츠] 콘텐츠 조회/검색/상세/히스토리/난이도 평가 API", description = "콘텐츠 관련 API")
public interface ContentControllerDocs {

    @GetExploreContentDocs
    SuccessResponse<Map<String, List<ContentResponse>>> getExploreContent(
            @CurrentUserId Long userId,
            int page
    );

    @GetTodayContentDocs
    SuccessResponse<List<ContentResponse>> getTodayContent(
            @CurrentUserId Long userId
    );

    @GetContentDetailDocs
    SuccessResponse<ContentResponse> getContentDetail(
            int contentId
    );

    @SearchContentDocs
    SuccessResponse<List<ContentResponse>> searchContent(
            @CurrentUserId Long userId,
            String keyword,
            int page
    );

    @SetContentEvaluationDocs
    SuccessResponse<DifficultyRecommendResponse> setContentEvaluation(
            @CurrentUserId Long userId,
            int contentId,
            @RequestBody @Valid ContentDifficultyRequest difficulty
    );

    @SetContentReadDocs
    SuccessResponse<Void> setContentRead(
            @CurrentUserId Long userId,
            int contentId
    );

    @SetContentReadDocs
    SuccessResponse<ReadContentDetailResponse> getReadDetail(
            @CurrentUserId Long userId,
            int contentId
    );
}