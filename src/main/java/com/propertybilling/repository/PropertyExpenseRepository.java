package com.propertybilling.repository;

import com.propertybilling.dto.expense.queryresult.ExpenseIndexQueryResult;
import com.propertybilling.entity.PropertyExpense;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for property expense records.
 */
public interface PropertyExpenseRepository extends JpaRepository<PropertyExpense, UUID> {

	/**
	 * Finds property expenses for one property and optional month.
	 *
	 * @param propertyId owning property identifier
	 * @param monthStart optional first day of the filtered month
	 * @param nextMonthStart optional first day after the filtered month
	 * @param offset number of rows to skip
	 * @param limit maximum rows to return
	 * @return matching expense rows ordered by newest expense date first
	 */
	@Query(value = """
			SELECT
			    expense.id,
			    expense.property_id AS propertyId,
			    expense.expense_date AS expenseDate,
			    expense.category,
			    expense.amount,
			    expense.description
			FROM property_expenses expense
			WHERE expense.property_id = :propertyId
			AND (
			    CAST(:monthStart AS DATE) IS NULL
			    OR (
			        expense.expense_date >= CAST(:monthStart AS DATE)
			        AND expense.expense_date < CAST(:nextMonthStart AS DATE)
			    )
			)
			ORDER BY
			    expense.expense_date DESC,
			    expense.category ASC,
			    expense.id ASC
			OFFSET :offset
			LIMIT :limit
			""", nativeQuery = true)
	List<ExpenseIndexQueryResult> findIndex(
			@Param("propertyId") UUID propertyId,
			@Param("monthStart") LocalDate monthStart,
			@Param("nextMonthStart") LocalDate nextMonthStart,
			@Param("offset") int offset,
			@Param("limit") int limit
	);

	/**
	 * Finds one expense using a write lock for mutation workflows.
	 *
	 * @param expenseId property expense identifier
	 * @return matching expense, or empty when it does not exist
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT expense
			FROM PropertyExpense expense
			WHERE expense.id = :expenseId
			""")
	Optional<PropertyExpense> findByIdForUpdate(@Param("expenseId") UUID expenseId);
}
