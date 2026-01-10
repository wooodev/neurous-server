package com.example.server.domain.content.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.server.domain.content.entity.Content;
import com.example.server.domain.content.entity.vo.ContentCategory;
import com.example.server.domain.content.entity.vo.ContentLevel;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

	//batchTime 조회 * 최신
	@Query(" SELECT MAX(c.batchTime) FROM Content c")
	LocalDateTime findLatestBatchTime();

	//최신 배치 + 카테고리 + 10개 * 카테고리별 10개
	@Query("""
		    SELECT c FROM Content c
		    WHERE c.contentLevel = :level
		      AND c.contentCategory = :category
		      AND c.batchTime = :batchTime
		    ORDER BY c.contentId DESC
		""")
	List<Content> findLatestBatchContents(
		@Param("level") ContentLevel level,
		@Param("category") ContentCategory category,
		@Param("batchTime") LocalDateTime batchTime,
		Pageable pageable
	);

	@Query("""
		    SELECT c FROM Content c
		    WHERE c.contentLevel = :level
		      AND c.title LIKE %:keyword%
		    ORDER BY c.contentId DESC
		""")
	List<Content> searchByTitle(
		@Param("level") ContentLevel level,
		@Param("keyword") String keyword,
		Pageable pageable
	);

	boolean existsByNewsArticleIdAndContentLevel(Long newsArticleId, ContentLevel contentLevel);

	@Query("""
		SELECT c FROM Content c
		WHERE c.contentLevel = :level
		  AND c.contentCategory = :category
		ORDER BY c.batchTime DESC, c.contentId DESC
	""")
	List<Content> findByCategoryAndLevel(
			@Param("level") ContentLevel level,
			@Param("category") ContentCategory category,
			Pageable pageable
	);
}
