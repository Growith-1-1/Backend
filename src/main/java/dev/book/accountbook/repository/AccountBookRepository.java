package dev.book.accountbook.repository;

import dev.book.accountbook.dto.response.AccountBookStatResponse;
import dev.book.accountbook.dto.response.AccountBookWeekConsumePerUserResponse;
import dev.book.accountbook.entity.AccountBook;
import dev.book.accountbook.repository.querydsl.account.AccountBookRepositoryCustom;
import dev.book.accountbook.type.CategoryType;
import dev.book.challenge.rank.dto.response.RankResponse;
import dev.book.global.entity.Category;
import dev.book.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountBookRepository extends JpaRepository<AccountBook, Long>, AccountBookRepositoryCustom {
    Optional<AccountBook> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"user", "category"})
    @Query("""
            SELECT a
            FROM AccountBook a
                WHERE a.user.id = :userId
                    AND a.occurredAt BETWEEN :startDate AND :endDate
                ORDER BY a.occurredAt DESC
            """)
    Page<AccountBook> findAllPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
                SELECT new dev.book.accountbook.dto.response.AccountBookStatResponse(c.korean, SUM(ab.amount))
                FROM AccountBook ab
                    JOIN ab.category c
                    WHERE ab.user.id = :userId
                      AND ab.occurredAt BETWEEN :startDate AND :endDate
                      AND ab.type = :categoryType
                    GROUP BY c.korean
                    ORDER BY SUM(ab.amount) DESC
            """)
    List<AccountBookStatResponse> findTopCategoriesByUserAndPeriod(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("categoryType") CategoryType categoryType
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
    Page<AccountBook> findByCategory(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("categoryName") String categoryName,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    @Query("""
                SELECT COALESCE(SUM(a.amount), 0)
                FROM AccountBook a
                WHERE a.user.id = :userId
                  AND a.type = :categoryType
                  AND a.occurredAt BETWEEN :startDate AND :endDate
            """)
    Integer sumSpending(
            @Param("userId") Long userId,
            @Param("categoryType") CategoryType categoryType,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
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
    Page<AccountBook> findByUserIdAndCategoryNameWithGraph(
            @Param("userId") Long userId,
            @Param("categoryId") Long categoryId,
            Pageable pageable
    );


    @Query("""
                SELECT new dev.book.challenge.rank.dto.response.RankResponse(
                    u.name,
                    COALESCE(SUM(ab.amount), 0)
                )
                FROM UserEntity u
                LEFT JOIN AccountBook ab ON ab.user = u
                    AND ab.type = 'SPEND'
                    AND ab.occurredAt BETWEEN :startDate AND :endDate
                    AND ab.category IN :categories
                WHERE u.id IN :participantIds
                GROUP BY u.id, u.name
                ORDER BY COALESCE(SUM(ab.amount), 0) ASC
            """)
    List<RankResponse> findByUserSpendingRanks(List<Long> participantIds, List<Category> categories, LocalDate startDate, LocalDate endDate);

    @Query("""
                SELECT a
                FROM AccountBook a
                JOIN FETCH a.category
                WHERE a.user.id = :userId 
                            AND a.occurredAt BETWEEN :start AND :end
                ORDER BY a.occurredAt DESC
            """)
    List<AccountBook> findAllMonth(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("""
            SELECT new dev.book.accountbook.dto.response.AccountBookWeekConsumePerUserResponse(
                u,
                COALESCE(SUM(CASE WHEN a.occurredAt BETWEEN :startDate AND :endDate THEN a.amount END), 0L),
                COALESCE(SUM(CASE WHEN a.occurredAt BETWEEN :lastStartDate AND :lastEndDate THEN a.amount END), 0L)
            ) 
            FROM AccountBook a
            JOIN a.user u
            WHERE a.type = 'SPEND'
            GROUP BY u.id
            """)
    List<AccountBookWeekConsumePerUserResponse> findUserAndAmountByConsumeOfWeek(
                                                    @Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("lastStartDate") LocalDate lastStartDate,
                                                    @Param("lastEndDate") LocalDate lastEndDate
    );

    @Query("""
                SELECT COALESCE(SUM(a.amount), 0)
                FROM AccountBook a
                JOIN a.category c
                WHERE a.user.id = :userId
                  AND a.type = :categoryType
                  AND a.category IN :categories
                  AND a.occurredAt BETWEEN :startDate AND :endDate
            """)
    Integer sumSpendingInCategories(@Param("userId") Long userId,
                                    @Param("categoryType") CategoryType categoryType,
                                    @Param("categories") List<Category> category,
                                    @Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate
    );

    void deleteAllByUser(UserEntity user);
}
