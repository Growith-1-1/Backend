package dev.book.global.repository;

import dev.book.global.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByCategoryIn(List<String> categories);

    Optional<Category> findByKorean(String korean);

    Optional<Category> findByCategory(String getCategory);
}
