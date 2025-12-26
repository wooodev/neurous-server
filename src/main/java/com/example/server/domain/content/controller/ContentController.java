package com.example.server.domain.content.controller;

import com.example.server.domain.content.controller.docs.ContentControllerDocs;
import com.example.server.domain.content.dto.ContentResponse;
import com.example.server.domain.content.dto.ContentDifficultyRequest;
import com.example.server.domain.content.dto.DifficultyRecommendResponse;
import com.example.server.domain.content.service.ContentService;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;
import com.example.server.global.security.annotation.PublicApi;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController implements ContentControllerDocs {

    private final ContentService contentService;

    @AuthenticatedApi
    @GetMapping("/explore")
    public SuccessResponse<Map<String, List<ContentResponse>>> getExploreContent(
            @CurrentUserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        Map<String, List<ContentResponse>> result = contentService.getExploreContent(userId, page);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }

    @AuthenticatedApi
    @GetMapping("/today")
    public SuccessResponse<List<ContentResponse>> getTodayContent(
            @CurrentUserId Long userId
    ) {
        List<ContentResponse> result = contentService.getTodayContent(userId);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }

    @PublicApi
    @GetMapping("/detail")
    public SuccessResponse<ContentResponse> getContentDetail(
            @PathVariable int contentId
    ) {
        ContentResponse contentResponse = contentService.getContentDetail(contentId);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, contentResponse);
    }

    @AuthenticatedApi
    @GetMapping("/search")
    public SuccessResponse<List<ContentResponse>> searchContent(
            @CurrentUserId Long userId,
            @RequestParam("keyword") String keyword,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        List<ContentResponse> result = contentService.search(userId, keyword, page);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }

    @AuthenticatedApi
    @GetMapping("/history")
    public SuccessResponse<List<ContentResponse>> getReadHistory(
            @CurrentUserId Long userId,
            @RequestParam(value = "page", defaultValue = "0") int page
    ) {
        List<ContentResponse> result = contentService.getReadHistory(userId, page);
        return SuccessResponse.of(SuccessMessage.LOAD_SUCCESS, result);
    }

    @AuthenticatedApi
    @PostMapping("/evaluation")
    public SuccessResponse<DifficultyRecommendResponse> setContentEvaluation(
            @CurrentUserId Long userId,
            @PathVariable int contentId,
            @RequestBody ContentDifficultyRequest difficulty
    ) {
        DifficultyRecommendResponse result = contentService.setDifficultyEvaluation(userId, contentId, difficulty);
        return SuccessResponse.of(SuccessMessage.UPDATE_SUCCESS, result);
    }

    @AuthenticatedApi
    @PostMapping("/{contentId}/read")
    public SuccessResponse<Void> setContentRead(
            @CurrentUserId Long userId,
            @PathVariable int contentId
    ) {
        contentService.setContentRead(userId, contentId);
        return SuccessResponse.of(SuccessMessage.UPDATE_SUCCESS);
    }

}