package com.example.server.global.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@Getter
public class StorageConfig {

	@Value("${gcs.object-storage.public-base-url}")
	private String baseUrl;

	@Value("${gcs.object-storage.profile-path}")
	private String profilePath;

	@Value("${gcs.object-storage.character-path}")
	private String characterPath;

	public String getProfileUrl(String fileName) {
		if (fileName == null)
			return null;
		return baseUrl + profilePath + fileName;
	}
	
	//동영상 형식 캐릭터
	public String getCharacterUrl(String folderName) {
		if (folderName == null)
			return null;
		return baseUrl + characterPath + folderName + "/";
	}
}
