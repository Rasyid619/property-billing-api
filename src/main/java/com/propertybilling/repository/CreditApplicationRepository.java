package com.propertybilling.repository;

import com.propertybilling.entity.CreditApplication;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for credit applications.
 */
public interface CreditApplicationRepository extends JpaRepository<CreditApplication, UUID> {

	/**
	 * Sums all credit applied to an invoice.
	 *
	 * @param invoiceId invoice identifier
	 * @return total applied credit or zero when no credit was applied
	 */
	@Query(value = """
			SELECT COALESCE(SUM(CAST(amount AS NUMERIC)), 0)
			FROM credit_applications
			WHERE invoice_id = :invoiceId
			""", nativeQuery = true)
	BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
