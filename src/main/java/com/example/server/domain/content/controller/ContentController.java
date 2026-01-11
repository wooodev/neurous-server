package com.example.server.domain.content.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.server.domain.content.controller.docs.ContentControllerDocs;
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
import com.example.server.domain.content.service.ContentService;
import com.example.server.domain.quiz.dto.response.ReadContentDetailResponse;
import com.example.server.domain.user.entity.vo.Level;
import com.example.server.global.annotation.CurrentUserId;
import com.example.server.global.exception.dto.SuccessResponse;
import com.example.server.global.exception.message.SuccessMessage;
import com.example.server.global.security.annotation.AuthenticatedApi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/content")
public class ContentController implements ContentControllerDocs {

	private final ContentService contentService;

	@AuthenticatedApi(reason = "사용자의 학습 레벨에 맞는 컨텐츠 탐색을 위해 로그인 필요")
	@GetMapping("/explore")
	public SuccessResponse<ExploreResponse> getExploreContent(
		@CurrentUserId Long userId,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return SuccessResponse.of(
				SuccessMessage.LOAD_CONTENT_EXPLORE_SUCCESS,
				contentService.getExplore(userId, page, size)
		);
	}

	@AuthenticatedApi(reason = "사용자의 학습 레벨과 사용자가 선택한 카테고리 컨텐츠 탐색을 위해 로그인 필요")
	@GetMapping("/explore/{category}")
	public SuccessResponse<ExploreResponse> getExploreContentByCategory(
		@CurrentUserId Long userId,
		@PathVariable ContentCategory category,
		@RequestParam(defaultValue = "0") int page,
		@RequestParam(defaultValue = "10") int size
	) {
		return SuccessResponse.of(
				SuccessMessage.LOAD_CONTENT_EXPLORE_BY_CATEGORY_SUCCESS,
				contentService.getExploreByCategory(userId, category, page, size)
		);
	}

	@AuthenticatedApi(reason = "컨텐츠 상세 조회를 위해 로그인 필요")
	@GetMapping("/{contentId}")
	public SuccessResponse<ContentDetailResponse> getContentDetail(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	) {
		return SuccessResponse.of(
				SuccessMessage.LOAD_CONTENT_DETAIL_SUCCESS,
				contentService.getContentDetailWithCount(userId, contentId)
		);
	}

	@AuthenticatedApi(reason = "읽은 컨텐츠 상세 조회를 위해 로그인 필요")
	@GetMapping("/{contentId}/read")
	public SuccessResponse<ReadContentDetailResponse> getReadContentDetail(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	) {
		return SuccessResponse.of(
				SuccessMessage.LOAD_READ_CONTENT_DETAIL_SUCCESS,
				contentService.getReadContentDetail(userId, contentId)
		);
	}

	@AuthenticatedApi(reason = "컨텐츠 검색을 위해 로그인 필요")
	@GetMapping("/search")
	public SuccessResponse<List<ContentResponse>> searchContent(
		@CurrentUserId Long userId,
		@RequestParam String keyword,
		@RequestParam(defaultValue = "0") int page
	) {
		return SuccessResponse.of(
				SuccessMessage.SEARCH_CONTENT_SUCCESS,
				contentService.search(userId, keyword, page)
		);
	}

	@AuthenticatedApi(reason = "컨텐츠 접근 권한 확인을 위해 로그인이 필요합니다")
	@GetMapping("/{contentId}/access")
	public SuccessResponse<ContentAccessResponse> checkContentAccess(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	) {
		return SuccessResponse.of(
				SuccessMessage.CHECK_CONTENT_ACCESS_SUCCESS,
				contentService.checkContentReadAccess(userId, contentId)
		);
	}

	@AuthenticatedApi(reason = "컨텐츠 읽기 상태 업데이트를 위해 로그인이 필요합니다")
	@PostMapping("/{contentId}/read-status")
	public SuccessResponse<ReadStatusResponse> updateReadStatus(
		@CurrentUserId Long userId,
		@PathVariable Long contentId,
		@Valid @RequestBody UpdateReadStatusRequest request,
		@RequestParam(defaultValue = "false") boolean isFromMission
	) {
		return SuccessResponse.of(
				SuccessMessage.UPDATE_READ_STATUS_SUCCESS,
				contentService.updateReadStatus(
					userId,
					contentId,
					request,
					isFromMission
				)
		);
	}

	@AuthenticatedApi(reason = "포인트 결제를 위해 로그인 필요합니다.")
	@PostMapping("/{contentId}/purchase/point")
	public SuccessResponse<Void> purchaseByPoint(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	) {
		contentService.purchaseContentByPoint(userId, contentId);
		return SuccessResponse.of(SuccessMessage.PURCHASE_CONTENT_SUCCESS);
	}

	@AuthenticatedApi(reason = "광고 시청을 위해 로그인 필요합니다")
	@PostMapping("/{contentId}/purchase/ad")
	public SuccessResponse<Void> unlockByWatchingAd(
		@CurrentUserId Long userId,
		@PathVariable Long contentId
	) {
		contentService.watchAdAndRewardUnLockContent(userId, contentId);
		return SuccessResponse.of(SuccessMessage.UNLOCK_CONTENT_BY_AD_SUCCESS);
	}

	@AuthenticatedApi(reason = "난이도 추천을 위해 로그인 필요합니다")
	@GetMapping("/difficulty/recommend")
	public SuccessResponse<DifficultyRecommendResponse> recommendDifficulty(
		@CurrentUserId Long userId
	) {
		return SuccessResponse.of(
			SuccessMessage.RECOMMEND_CONTENT_DIFFICULTY_SUCCESS,
			contentService.requestRecommendContentLevel(userId)
		);
	}

	@AuthenticatedApi(reason = "레벨 변경을 위해 로그인 필요합니다")
	@PostMapping("/difficulty/change")
	public SuccessResponse<Void> changeUserLevel(
		@CurrentUserId Long userId,
		@RequestParam Level level
	) {
		contentService.changeUserLevel(userId, level);
		return SuccessResponse.of(SuccessMessage.CHANGE_LEVEL_SUCCESS);
	}

	@AuthenticatedApi(reason = "난이도 평가를 위해 로그인 필요합니다")
	@PostMapping("/{contentId}/difficulty")
	public SuccessResponse<Void> evaluateDifficulty(
		@CurrentUserId Long userId,
		@PathVariable Long contentId,
		@RequestParam ContentDifficulty difficulty
	) {
		contentService.contentDifficultyAssessment(userId, contentId, difficulty);
		return SuccessResponse.of(SuccessMessage.EVALUATE_CONTENT_DIFFICULTY_SUCCESS);
	}
}
