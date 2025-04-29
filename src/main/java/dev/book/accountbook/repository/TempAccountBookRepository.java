package dev.book.accountbook.repository;

import dev.book.accountbook.entity.TempAccountBook;
import dev.book.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempAccountBookRepository extends JpaRepository<TempAccountBook, Long> {
    List<TempAccountBook> findAllByUserId(Long userId);
    void deleteAllByUser(UserEntity user);
}
