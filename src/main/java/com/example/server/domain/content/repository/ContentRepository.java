package com.example.server.domain.content.repository;

import com.example.server.domain.content.entity.Content;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Integer> {
    List<Content> findByContentDiffAndContentCategory(String contentDiff, String contentCategory, Pageable pageable);

    List<Content> findByContentDiffOrderByContentIdDesc(String contentDiff, Pageable pageable);

    @Query(value = """
            SELECT *
            FROM content
            WHERE content_diff = :ContentDiff
              AND content_category = :category
              AND NOT EXISTS (
                                SELECT 1
                                FROM read_content rc
                                WHERE rc.user_id = :userId
                                  AND rc.content_id = c.content_id
                            )
            ORDER BY RAND()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Content> findRandomUnreadByContentDiffAndCategory(Long userId, String ContentDiff, String category);

    @Query(value = """
            SELECT *
            FROM content
            WHERE content_diff = :ContentDiff
              AND content_category = :category
              AND content_id NOT IN (:excludedIds)
              AND NOT EXISTS (
                                SELECT 1
                                FROM read_content rc
                                WHERE rc.user_id = :userId
                                  AND rc.content_id = c.content_id
                            )
            ORDER BY RAND()
            LIMIT 1
            """, nativeQuery = true)
    Optional<Content> findRandomUnreadByContentDiffAndCategoryExcludeIds(Long userId, String ContentDiff, String category, List<Integer> excludedIds);

    @Query(value = """
            SELECT *
            FROM content c
            WHERE c.content_diff = :contentDiff
              AND LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
            ORDER BY c.content_id DESC
            """, nativeQuery = true)
    List<Content> searchByTitle(String contentDiff, String keyword, Pageable pageable);

    boolean existsByTitleAndContentDiff(String title, String contentDiff);

    boolean existsByNewsArticleIdAndContentDiff(Long newsArticleId, String contentDiff);
}

