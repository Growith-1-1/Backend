package dev.book.accountbook.repository;

import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.type.CategoryType;
import dev.book.challenge.rank.dto.response.RankResponse;
import dev.book.global.entity.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long> {
    Optional<AccountBook> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT a
            FROM AccountBook a
                JOIN a.category c
                WHERE a.user.id = :userId
                    AND a.type = :categoryType
                    AND a.occurredAt BETWEEN :startDate AND :endDate
                ORDER BY a.occurredAt DESC
            """)
    List<AccountBook> findAllByTypeAndPeriod(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("""
            SELECT a
            FROM AccountBook a
                JOIN a.category c
                WHERE a.user.id = :userId
                    AND a.occurredAt BETWEEN :startDate AND :endDate
                ORDER BY a.occurredAt DESC
            """)
    List<AccountBook> findAllPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
                SELECT new dev.book.accountbook.dto.response.AccountBookStatResponse(c.korean, SUM(ab.amount))
                FROM AccountBook ab
                    JOIN ab.category c
                    WHERE ab.user.id = :userId
                      AND ab.type = :categoryType
                      AND ab.occurredAt BETWEEN :startDate AND :endDate
                    GROUP BY c.korean
                    ORDER BY SUM(ab.amount) DESC
            """)
    List<AccountBookStatResponse> findTopCategoriesByUserAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("categoryType") CategoryType categoryType,
            Pageable pageable
    );

    @Query("""
                SELECT a
                FROM AccountBook a
                JOIN a.category c
                WHERE a.user.id = :userId
                  AND a.type = :categoryType
                  AND c.category = :categoryName
                  AND a.occurredAt BETWEEN :startDate AND :endDate
                ORDER BY a.occurredAt DESC
            """)
    List<AccountBook> findByCategory(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("categoryName") String categoryName,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
                SELECT COALESCE(SUM(a.amount), 0)
                FROM AccountBook a
                JOIN a.category c
                WHERE a.user.id = :userId
                  AND a.type = :categoryType
                  AND a.occurredAt BETWEEN :startDate AND :endDate
            """)
    Integer sumSpending(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @EntityGraph(attributePaths = {"category"})
    @Query("""
                SELECT DISTINCT ab
                FROM AccountBook ab
                JOIN ab.category c
                WHERE ab.user.id = :userId
                  AND c.id = :categoryId
                ORDER BY ab.occurredAt DESC, ab.id DESC
            """)
    List<AccountBook> findByUserIdAndCategoryNameWithGraph(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


    @Query("""
    SELECT new dev.book.challenge.rank.dto.response.RankResponse(
        u.name,
        COALESCE(SUM(ab.amount), 0L)
    )
    FROM UserEntity u
    LEFT JOIN AccountBook ab ON ab.user = u 
        AND ab.type = 'SPEND'
        AND ab.endDate BETWEEN :startDate AND :endDate
    LEFT JOIN ab.category c
    WHERE u.id IN :participantIds
      AND (
        c IN :categories
        OR ab IS NULL
      )
    GROUP BY u.id, u.name
    ORDER BY COALESCE(SUM(ab.amount), 0L) ASC
""")
    List<RankResponse> findByUserSpendingRanks(List<Long> participantIds, List<Category> categories, LocalDateTime startDate, LocalDateTime endDate);

    @Query("""
                SELECT a
                FROM AccountBook a
                JOIN FETCH a.category
                WHERE a.user.id = :userId 
                            AND a.occurredAt BETWEEN :start AND :end
                ORDER BY a.occurredAt DESC
            """)
    List<AccountBook> findAllMonth(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);
}

