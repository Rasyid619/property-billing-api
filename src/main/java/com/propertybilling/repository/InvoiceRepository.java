package com.propertybilling.repository;

import com.propertybilling.entity.Invoice;
import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for invoice records.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

	/**
	 * Checks whether any unit already has an invoice for the billing month.
	 *
	 * @param unitIds units to check
	 * @param billingMonth first day of the billing month
	 * @return true when any matching invoice already exists
	 */
	@Query("""
			SELECT CASE WHEN COUNT(invoice) > 0 THEN true ELSE false END
			FROM Invoice invoice
			WHERE invoice.unit.id IN :unitIds
			AND invoice.billingMonth = :billingMonth
			""")
	boolean existsByUnitIdsAndBillingMonth(
			@Param("unitIds") Collection<UUID> unitIds,
			@Param("billingMonth") LocalDate billingMonth
	);
}
