package com.example.server.domain.user.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.server.domain.auth.enums.OAuthProvider;
import com.example.server.domain.user.entity.User;
import com.example.server.domain.user.entity.vo.UserInterest;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findById(Long userId);

	Optional<User> findByProviderAndProviderId(OAuthProvider provider, String providerId);

	@Query("select u.level from User u where u.id = :userId")
	Optional<Object> findLevelByUserId(Long userId);

	@Modifying(clearAutomatically = true)
	@Query("""
		UPDATE User u 
		SET u.point = u.point + :rewardPoint ,
				u.exp = u.exp + :rewardExp
		WHERE u.attendanceCount >= 7 
		""")
	int rewardAttendanceAllWeek(
		@Param("rewardPoint") int rewardPoint,
		@Param("rewardExp") int rewardExp
	);

	//모든 유저의 출석 카운트 0 으로 초기화
	@Modifying(clearAutomatically = true)
	@Query(" UPDATE User u SET u.attendanceCount =0")
	void resetAllAttendance();

	//유저 관심사 추출
	@Query("""
		    SELECT ui FROM UserInterest ui 
		    JOIN FETCH ui.user 
		    WHERE ui.user.id = :userId 
		    ORDER BY ui.priority ASC
		""")
	List<UserInterest> findAllInterestsByUserId(@Param("userId") Long userId);

	List<User> findByNotificationStatusTrue();
}
