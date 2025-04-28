package dev.book.achievement.achievement_user.repository;

import dev.book.achievement.achievement_user.entity.IndividualAchievementStatus;
import dev.book.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IndividualAchievementStatusRepository extends JpaRepository<IndividualAchievementStatus, Long> {
    Optional<IndividualAchievementStatus> findByUser(UserEntity user);

    void deleteByUser(UserEntity user);
}
