package dev.book.accountbook.repository;

import dev.book.accountbook.entity.Budget;
import dev.book.accountbook.repository.querydsl.BudgetRepositoryCustom;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> , BudgetRepositoryCustom {
    Optional<Budget> findByIdAndUserId(Long id, Long userId);
    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = {"user"})
    @Query("""
        SELECT b FROM Budget b JOIN FETCH UserEntity u WHERE b.month=:month
    """)
    List<Budget> findAllByMonthWithUser(@Param("month") int month);
}
