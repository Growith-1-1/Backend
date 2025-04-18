package dev.book.accountbook.repository;

import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.Category;
import dev.book.accountbook.type.CategoryType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
    Optional<AccountBook> findByIdAndUserId(Long id, Long userId);

    List<AccountBook> findAllByUserIdAndTypeOrderByUpdatedAtDesc(Long userId, CategoryType type);

    @Query("""
            SELECT new dev.book.accountbook.dto.response.AccountBookStatResponse(a.category, SUM(a.amount))
                        FROM AccountBook a
                        WHERE a.user.id = :userId
                        AND a.updatedAt BETWEEN :startDate AND :endDate
                        GROUP BY a.category
                        ORDER BY SUM(a.amount) DESC
            """)
    List<AccountBookStatResponse> findTop3Categories(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("""
              SELECT a
                  FROM AccountBook a
                  WHERE a.user.id = :userId
                  AND a.type = :categoryType
                  AND a.category = :category
            AND a.updatedAt BETWEEN :startDate AND :endDate
                        ORDER BY a.updatedAt DESC
            """)
    List<AccountBook> findByCategory(Long userId, CategoryType categoryType, Category category, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
                SELECT COALESCE(SUM(a.amount), 0)
                    FROM AccountBook a
                    WHERE a.user.id = :userId
                    AND a.type = :categoryType
                    AND a.category = :category
                    AND a.updatedAt BETWEEN :startDate AND :endDate
            """)
    Integer sumSpending(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("category") Category category,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    List<AccountBook> findByUserIdAndCategoryOrderByUpdatedAtDescIdDesc(Long userId, Category category);
}
