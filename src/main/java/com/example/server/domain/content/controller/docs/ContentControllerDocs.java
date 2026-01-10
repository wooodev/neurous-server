package com.example.server.domain.content.controller.docs;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.server.domain.content.dto.request.UpdateReadStatusRequest;
import com.example.server.domain.content.dto.response.ContentAccessResponse;
import com.example.server.domain.content.dto.response.ContentDetailResponse;
import com.example.server.domain.content.dto.response.ContentResponse;
import com.example.server.domain.content.dto.response.DifficultyRecommendResponse;
import com.example.server.domain.content.dto.response.ExploreResponse;
import com.example.server.domain.content.dto.response.ReadStatusResponse;
import com.example.server.domain.content.dto.response.RecentSearchResponse;
import com.example.server.domain.content.entity.vo.ContentCategory;
import com.example.server.domain.content.entity.vo.ContentDifficulty;
import com.example.server.domain.quiz.dto.response.ReadContentDetailResponse;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@Tag(name = "[컨텐츠] 컨텐츠 탐색 / 조회 / 구매 / 난이도 관련 API", description = "컨ㅋ텐츠 관련 API")
public interface ContentControllerDocs {

	@GetExploreDocs
	SuccessResponse<Map<ContentCategory, ExploreResponse>> getExploreContent(
		@CurrentUserId Long userId
	);

	@GetExploreContetsByCategoryDocs
	SuccessResponse<ExploreResponse> getExploreContentByCategory(
		@CurrentUserId Long userId,
		@PathVariable ContentCategory category
	);

	@GetContentDetailDocs
	SuccessResponse<ContentDetailResponse> getContentDetail(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	);

	@GetReadContentDetailDocs
	SuccessResponse<ReadContentDetailResponse> getReadContentDetail(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	);

	@SearchContentDocs
	SuccessResponse<List<ContentResponse>> searchContent(
		@CurrentUserId Long userId,
		@RequestParam String keyword,
		@RequestParam(defaultValue = "0") int page
	);

	@CheckContentAccessDocs
	SuccessResponse<ContentAccessResponse> checkContentAccess(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	);

	@UpdateReadStatusDocs
	SuccessResponse<ReadStatusResponse> updateReadStatus(
		@CurrentUserId Long userId,
		@PathVariable Long contentId,
		@Valid @RequestBody UpdateReadStatusRequest request,
		@RequestParam(defaultValue = "false") boolean isFromMission
	);

	@PurchaseContentByPointDocs
	SuccessResponse<Void> purchaseByPoint(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	);

	@UnlockContentByAdDocs
	SuccessResponse<Void> unlockByWatchingAd(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	);

	@RecommendDifficultyDocs
	SuccessResponse<DifficultyRecommendResponse> recommendDifficulty(
		@CurrentUserId Long userId
	);

	@ChangeUserLevelDocs
	SuccessResponse<Void> changeUserLevel(
		@CurrentUserId Long userId,
		@RequestParam Level level
	);

	@EvaluateContentDifficultyDocs
	SuccessResponse<Void> evaluateDifficulty(
		@CurrentUserId Long userId,
		@PathVariable Long contentId,
		@RequestParam ContentDifficulty difficulty
	);

}
