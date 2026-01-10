package com.example.server.domain.content.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExploreResponse {

	private List<ContentResponse> contents;

}
