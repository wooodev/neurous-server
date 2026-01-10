package com.example.server.domain.content.dto.response;

import java.time.LocalDate;

import com.example.server.domain.content.entity.Content;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ContentResponse {
	private Long contentId;
	private String title;
	private String categoryName;
	private String imgUrl;
	private int hits;
	private int readingTime;
	private LocalDate publishedDate;

	public static ContentResponse from(Content content, int redisHits) {
		int calculatedTime = (int)Math.ceil(content.getContent().length() / 500.0);

		return ContentResponse.builder()
			.contentId(content.getContentId())
			.title(content.getTitle())
			.categoryName(content.getContentCategory().getDescription()) // "정치" 등 반환
			.imgUrl(content.getImageUrl())
			// 핵심: DB 조회수 + Redis 실시간 조회수 합산
			.hits(content.getHits() + redisHits)
			.readingTime(Math.max(1, calculatedTime))
			.publishedDate(content.getContentDate().toLocalDate()) // 년/월/일 변환
			.build();
	}
}
