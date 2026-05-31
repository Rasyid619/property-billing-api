package com.propertybilling.repository;

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
