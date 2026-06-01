package com.propertybilling.repository;

import com.propertybilling.entity.CashBalance;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for monthly cash balances.
 */
public interface CashBalanceRepository extends JpaRepository<CashBalance, UUID> {

	/**
	 * Checks whether a property month has already been closed.
	 *
	 * @param propertyId owning property identifier
	 * @param month first day of the month
	 * @return true when a closing already exists
	 */
	boolean existsByPropertyIdAndMonth(UUID propertyId, LocalDate month);

	/**
	 * Finds one closed cash balance by property and month.
	 *
	 * @param propertyId owning property identifier
	 * @param month first day of the closed month
	 * @return matching cash balance when it exists
	 */
	Optional<CashBalance> findByPropertyIdAndMonth(UUID propertyId, LocalDate month);

	/**
	 * Finds the previous month closing balance for one property.
	 *
	 * @param propertyId owning property identifier
	 * @param month first day of the previous month
	 * @return previous closing balance when it exists
	 */
	@Query("""
			SELECT cashBalance.closingBalance
			FROM CashBalance cashBalance
			WHERE cashBalance.property.id = :propertyId
			AND cashBalance.month = :month
			""")
	Optional<String> findClosingBalanceByPropertyIdAndMonth(
			@Param("propertyId") UUID propertyId,
			@Param("month") LocalDate month
	);
}
