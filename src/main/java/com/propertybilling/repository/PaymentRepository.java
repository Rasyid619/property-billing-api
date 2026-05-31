package com.propertybilling.repository;

import com.propertybilling.dto.payment.queryresult.PaymentIndexQueryResult;
import com.propertybilling.entity.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for payment records.
 */
public interface PaymentRepository extends JpaRepository<Payment, UUID> {

	/**
	 * Sums all payments recorded for an invoice.
	 *
	 * @param invoiceId invoice identifier
	 * @return total paid amount or zero when no payment exists
	 */
	@Query(value = """
			SELECT COALESCE(SUM(CAST(amount AS NUMERIC)), 0)
			FROM payments
			WHERE invoice_id = :invoiceId
			""", nativeQuery = true)
	BigDecimal sumAmountByInvoiceId(@Param("invoiceId") UUID invoiceId);

	/**
	 * Finds payments recorded for an invoice.
	 *
	 * @param invoiceId invoice identifier
	 * @return payment rows ordered by payment date and creation order
	 */
	@Query("""
			SELECT new com.propertybilling.dto.payment.queryresult.PaymentIndexQueryResult(
			    payment.id,
			    invoice.id,
			    payment.amount,
			    payment.paymentDate,
			    payment.paymentMethod,
			    payment.referenceNumber,
			    payment.note,
			    invoice.status
			)
			FROM Payment payment
			JOIN payment.invoice invoice
			WHERE invoice.id = :invoiceId
			ORDER BY payment.paymentDate ASC, payment.createdAt ASC, payment.id ASC
			""")
	List<PaymentIndexQueryResult> findIndexByInvoiceId(@Param("invoiceId") UUID invoiceId);
}
