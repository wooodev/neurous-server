package com.example.server.domain.character.dto;

import com.example.server.domain.user.metadata.entity.CharacterData;

public record LevelStandardInformation(
	String characterLevel,
	String characterName,
	String characterImgUrl,
	Integer exp,
	String lv1Message
) {
	public static LevelStandardInformation from(CharacterData characterData) {
		String message = null;
		if (1 == characterData.getLevel()) { //LV1 인 경우에만
			message = "처음 시작";
		}
		return new LevelStandardInformation(
			characterData.getCharacterLevel(),
			characterData.getCharacterName(),
			characterData.getCharacterImageUrl(),
			characterData.getExp(),
			message
		);
	}
}
