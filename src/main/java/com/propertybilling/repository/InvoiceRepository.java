package com.propertybilling.repository;

import com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult;
import com.propertybilling.entity.Invoice;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for invoice records.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

	/**
	 * Finds invoice index rows using optional query filters.
	 *
	 * @param propertyId optional owning property filter
	 * @param unitId optional unit filter
	 * @param tenantId optional tenant filter
	 * @param billingMonth optional billing month filter
	 * @param status optional invoice status filter
	 * @param pageable pagination settings
	 * @return matching rows ordered by newest billing month first
	 */
	@Query("""
			SELECT new com.propertybilling.dto.invoice.queryresult.InvoiceIndexQueryResult(
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
			JOIN unit.property property
			JOIN invoice.tenant tenant
			WHERE (
			    :propertyId IS NULL
			    OR property.id = :propertyId
			)
			AND (
			    :unitId IS NULL
			    OR unit.id = :unitId
			)
			AND (
			    :tenantId IS NULL
			    OR tenant.id = :tenantId
			)
			AND (
			    :billingMonth IS NULL
			    OR invoice.billingMonth = :billingMonth
			)
			AND (
			    :status IS NULL
			    OR invoice.status = :status
			)
			ORDER BY invoice.billingMonth DESC, invoice.dueDate ASC, invoice.invoiceNumber ASC, invoice.id ASC
			""")
	List<InvoiceIndexQueryResult> findIndex(
			@Param("propertyId") UUID propertyId,
			@Param("unitId") UUID unitId,
			@Param("tenantId") UUID tenantId,
			@Param("billingMonth") LocalDate billingMonth,
			@Param("status") String status,
			Pageable pageable
	);
}
