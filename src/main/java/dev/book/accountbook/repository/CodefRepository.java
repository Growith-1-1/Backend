package dev.book.accountbook.repository;

import dev.book.accountbook.entity.Codef;
import dev.book.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CodefRepository extends JpaRepository<Codef, Long> {
    Optional<Codef> findByUser(UserEntity user);

    @Query("SELECT c FROM Codef c JOIN c.user u WHERE FUNCTION('DATE', u.createdAt) < CURRENT_DATE")
    List<Codef> findAllCodefWithUserCreatedBeforeToday();
}
