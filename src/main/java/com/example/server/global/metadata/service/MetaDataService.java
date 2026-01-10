package com.example.server.global.metadata.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.server.domain.reward.metadata.entity.RewardData;
import com.example.server.domain.reward.metadata.entity.RewardInfoData;
import com.example.server.domain.reward.metadata.repository.RewardDataRepository;
import com.example.server.domain.reward.metadata.repository.RewardInfoDataRepository;
import com.example.server.domain.user.metadata.entity.CharacterData;
import com.example.server.domain.user.metadata.repository.CharacterDataRepository;
import com.example.server.global.exception.message.ErrorMessage;
import com.example.server.global.exception.model.NotFoundException;
import com.example.server.global.redis.RedisKey;
import com.example.server.global.redis.RedisUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class MetaDataService {

	private final CharacterDataRepository characterDataRepository;
	private final RewardDataRepository rewardDataRepository;
	private final RewardInfoDataRepository rewardInfoDataRepository;
	private final RedisUtil redisUtil;

	public List<CharacterData> getAllCharacters() {
		String key = RedisKey.CHARACTER_LIST.getPrefix();

		List<CharacterData> cached = redisUtil.get(key, List.class);
		if (cached != null)
			return cached;

		// 2. DB 조회
		List<CharacterData> dbList = characterDataRepository.findAllByOrderByLevelAsc();

		// 3. 캐시 저장
		redisUtil.set(key, dbList, RedisKey.CHARACTER_LIST.getTtl());
		return dbList;
	}

	public CharacterData getCharacterByLevel(Integer level) {
		String key = RedisKey.CHARACTER_ITEM.getFullKey(level);

		CharacterData cached = redisUtil.get(key, CharacterData.class);
		if (cached != null)
			return cached;

		CharacterData dbData = characterDataRepository.findByLevel(level)
			.orElseThrow(() -> new NotFoundException(ErrorMessage.CHARACTER_METADATA_NOT_FOUND));

		redisUtil.set(key, dbData, RedisKey.CHARACTER_ITEM.getTtl());
		return dbData;
	}

	public List<RewardData> getAllRewardData() {
		String key = RedisKey.REWARD_DATA_LIST.getPrefix();

		List<RewardData> cached = redisUtil.get(key, List.class);
		if (cached != null)
			return cached;

		List<RewardData> dbList = rewardDataRepository.findAll();
		redisUtil.set(key, dbList, RedisKey.REWARD_DATA_LIST.getTtl());
		return dbList;
	}

	public List<RewardInfoData> getAllRewardInfo() {
		String key = RedisKey.REWARD_INFO_LIST.getPrefix();

		List<RewardInfoData> cached = redisUtil.get(key, List.class);
		if (cached != null)
			return cached;

		List<RewardInfoData> dbList = rewardInfoDataRepository.findAll();
		redisUtil.set(key, dbList, RedisKey.REWARD_INFO_LIST.getTtl());
		return dbList;
	}
}
