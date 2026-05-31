package com.propertybilling.entity;

import com.propertybilling.constant.InvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "invoices")
/*
 * Persisted monthly invoice for a tenant and unit at billing time.
 */
public class Invoice {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private Unit unit;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(name = "billing_month", nullable = false)
	private LocalDate billingMonth;

	@Column(name = "invoice_number", nullable = false)
	private String invoiceNumber;

	@Column(name = "amount", nullable = false)
	private String amount;

	@Column(name = "due_date", nullable = false)
	private LocalDate dueDate;

	@Column(name = "status", nullable = false)
	private String status;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new invoice that relies on database-managed timestamps.
	 *
	 * @param id unique invoice identifier
	 * @param unit billed unit
	 * @param tenant billed tenant
	 * @param billingMonth first day of the billed month
	 * @param invoiceNumber unique invoice number
	 * @param amount decimal string invoice amount
	 * @param dueDate invoice due date
	 * @param status current invoice status
	 */
	public Invoice(
			UUID id,
			Unit unit,
			Tenant tenant,
			LocalDate billingMonth,
			String invoiceNumber,
			String amount,
			LocalDate dueDate,
			String status
	) {
		this(id, unit, tenant, billingMonth, invoiceNumber, amount, dueDate, status, null, null);
	}

	/**
	 * Creates a persisted invoice representation.
	 *
	 * @param id unique invoice identifier
	 * @param unit billed unit
	 * @param tenant billed tenant
	 * @param billingMonth first day of the billed month
	 * @param invoiceNumber unique invoice number
	 * @param amount decimal string invoice amount
	 * @param dueDate invoice due date
	 * @param status current invoice status
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public Invoice(
			UUID id,
			Unit unit,
			Tenant tenant,
			LocalDate billingMonth,
			String invoiceNumber,
			String amount,
			LocalDate dueDate,
			String status,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.unit = unit;
		this.tenant = tenant;
		this.billingMonth = billingMonth;
		this.invoiceNumber = invoiceNumber;
		this.amount = amount;
		this.dueDate = dueDate;
		this.status = status;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Updates invoice status after payment totals are recalculated.
	 *
	 * @param status recalculated invoice status
	 */
	public void updateStatus(InvoiceStatus status) {
		this.status = status.value();
	}
}
