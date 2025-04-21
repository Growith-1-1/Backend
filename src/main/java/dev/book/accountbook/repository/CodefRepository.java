package dev.book.accountbook.repository;

import dev.book.accountbook.entity.Codef;
import dev.book.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CodefRepository extends JpaRepository<Codef, Long> {
    Optional<Codef> findByUser(UserEntity user);
}
