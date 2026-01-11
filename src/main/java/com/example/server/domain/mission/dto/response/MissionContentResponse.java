package com.example.server.domain.mission.dto.response;

import java.time.LocalDate;

public record MissionContentResponse(
	String contentTile,
	String contentImg,
	String contentCategory,
	LocalDate contentDate,
	Long contentId
) {
}
