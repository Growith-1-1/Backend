package dev.book.accountbook.repository;

import dev.book.accountbook.entity.TempAccountBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TempAccountBookRepository extends JpaRepository<TempAccountBook, Long> {
    List<TempAccountBook> findByUserId(Long userId);
}
