package com.propertybilling.repository;

import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceShowQueryResult;
import com.propertybilling.entity.Invoice;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
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

	/**
	 * Finds invoice index rows with settlement totals using optional query filters.
	 *
	 * @param propertyId optional owning property filter
	 * @param unitId optional unit filter
	 * @param tenantId optional tenant filter
	 * @param billingMonth optional billing month filter
	 * @param status optional invoice status filter
	 * @param offset number of rows to skip
	 * @param limit maximum rows to return
	 * @return matching invoice rows ordered by newest billing month first
	 */
	@Query(value = """
			WITH payment_totals AS (
			    SELECT
			        payment.invoice_id,
			        SUM(CAST(payment.amount AS NUMERIC)) AS paid_amount
			    FROM payments payment
			    GROUP BY payment.invoice_id
			),
			credit_totals AS (
			    SELECT
			        credit_application.invoice_id,
			        SUM(CAST(credit_application.amount AS NUMERIC)) AS credit_applied_amount
			    FROM credit_applications credit_application
			    GROUP BY credit_application.invoice_id
			)
			SELECT
			    invoice.id,
			    invoice.unit_id AS unit_id,
			    invoice.tenant_id AS tenant_id,
			    invoice.billing_month AS billing_month,
			    invoice.invoice_number AS invoice_number,
			    invoice.amount,
			    COALESCE(payment_totals.paid_amount, 0) AS paid_amount,
			    COALESCE(credit_totals.credit_applied_amount, 0) AS credit_applied_amount,
			    invoice.due_date AS due_date,
			    invoice.status
			FROM invoices invoice
			JOIN units unit ON unit.id = invoice.unit_id
			LEFT JOIN payment_totals ON payment_totals.invoice_id = invoice.id
			LEFT JOIN credit_totals ON credit_totals.invoice_id = invoice.id
			WHERE (CAST(:propertyId AS UUID) IS NULL OR unit.property_id = CAST(:propertyId AS UUID))
			AND (CAST(:unitId AS UUID) IS NULL OR invoice.unit_id = CAST(:unitId AS UUID))
			AND (CAST(:tenantId AS UUID) IS NULL OR invoice.tenant_id = CAST(:tenantId AS UUID))
			AND (CAST(:billingMonth AS DATE) IS NULL OR invoice.billing_month = CAST(:billingMonth AS DATE))
			AND (CAST(:status AS TEXT) IS NULL OR invoice.status = CAST(:status AS TEXT))
			ORDER BY
			    invoice.billing_month DESC,
			    invoice.due_date ASC,
			    invoice.invoice_number ASC,
			    invoice.id ASC
			OFFSET :offset
			LIMIT :limit
			""", nativeQuery = true)
	List<InvoiceIndexQueryResult> findIndex(
			@Param("propertyId") UUID propertyId,
			@Param("unitId") UUID unitId,
			@Param("tenantId") UUID tenantId,
			@Param("billingMonth") LocalDate billingMonth,
			@Param("status") String status,
			@Param("offset") int offset,
			@Param("limit") int limit
	);

	/**
	 * Finds one invoice detail row by ID.
	 *
	 * @param invoiceId invoice identifier
	 * @return matching invoice when it exists
	 */
	@Query("""
			SELECT new com.propertybilling.dto.invoice.queryresult.InvoiceShowQueryResult(
			    invoice.id,
			    unit.id,
			    tenant.id,
			    invoice.billingMonth,
			    invoice.invoiceNumber,
			    invoice.amount,
			    invoice.dueDate,
			    invoice.status
			)
			FROM Invoice invoice
			JOIN invoice.unit unit
			JOIN invoice.tenant tenant
			WHERE invoice.id = :invoiceId
			""")
	Optional<InvoiceShowQueryResult> findShow(@Param("invoiceId") UUID invoiceId);

	/**
	 * Finds the selected invoice and same-tenant/unit open invoices that can receive payments or credit.
	 *
	 * @param selectedInvoiceId selected invoice that receives payment first
	 * @param statuses invoice statuses eligible for surplus allocation
	 * @return selected and open invoices ordered oldest first
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT invoice
			FROM Invoice invoice
			JOIN FETCH invoice.tenant
			JOIN FETCH invoice.unit
			WHERE invoice.tenant.id = (
			    SELECT selectedInvoice.tenant.id
			    FROM Invoice selectedInvoice
			    WHERE selectedInvoice.id = :selectedInvoiceId
			)
			AND invoice.unit.id = (
			    SELECT selectedInvoice.unit.id
			    FROM Invoice selectedInvoice
			    WHERE selectedInvoice.id = :selectedInvoiceId
			)
			AND (
			    invoice.id = :selectedInvoiceId
			    OR invoice.status IN :statuses
			)
			ORDER BY invoice.billingMonth ASC, invoice.dueDate ASC, invoice.invoiceNumber ASC, invoice.id ASC
			""")
	List<Invoice> findPaymentAllocationInvoicesForUpdate(
			@Param("selectedInvoiceId") UUID selectedInvoiceId,
			@Param("statuses") Collection<String> statuses
	);
}
