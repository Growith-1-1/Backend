package dev.book.achievement.achievement_user.repository;

import dev.book.achievement.achievement_user.entity.AchievementUser;
import dev.book.user.entity.UserEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AchievementUserRepository extends JpaRepository<AchievementUser, Long> {
    boolean existsByAchievementIdAndUserId(Long achievementId, Long userId);

    @EntityGraph(attributePaths = "achievement")
    List<AchievementUser> findAllByUser(UserEntity user);
}
