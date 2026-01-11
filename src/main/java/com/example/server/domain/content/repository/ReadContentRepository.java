package com.example.server.domain.content.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.server.domain.content.entity.ReadContent;

@Repository
public interface ReadContentRepository extends JpaRepository<ReadContent, Long> {

	boolean existsByUser_IdAndContent_ContentId(Long userId, Long contentId);

	@Query("SELECT rc FROM ReadContent rc JOIN FETCH rc.content WHERE rc.user.id = :userId")
	List<ReadContent> findReadContentsByUserId(@Param("userId") Long userId, Pageable pageable);

	Optional<ReadContent> findByUser_IdAndContent_ContentId(Long userId, Long contentId);

	//마이페이지용 사용자 데이터 조회 * 읽은 컨텐츠 + 퀴즈 정답 여부
	@Query("""
		select rc from ReadContent rc
		join fetch rc.content c
		left join fetch rc.quizSolve qs
		where rc.user.id = :userId
		  and rc.readAt between :startOfWeek and :endOfWeek
		order by rc.readAt desc
		""")
	List<ReadContent> findWeeklyHistory(
		@Param("userId") Long userId,
		@Param("startOfWeek") LocalDateTime startOfWeek, //시작주
		@Param("endOfWeek") LocalDateTime endOfWeek
	);

	//유저 정보 한번에 조회
	@Query("SELECT rc FROM ReadContent rc JOIN FETCH rc.user WHERE rc.readContentId = :id")
	Optional<ReadContent> findByIdWithUser(@Param("id") Long id);

	Optional<ReadContent> findTopByUser_IdAndContent_ContentIdOrderByReadAtDesc(Long userId, Long contentId);

}


