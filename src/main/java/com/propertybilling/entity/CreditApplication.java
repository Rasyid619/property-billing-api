package com.propertybilling.entity;

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
@Table(name = "credit_applications")
/*
 * Applied tenant/unit credit that settles an invoice without recording cash income.
 */
public class CreditApplication {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_unit_credit_id", nullable = false)
	private TenantUnitCredit tenantUnitCredit;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;

	@Column(name = "amount", nullable = false)
	private String amount;

	@Column(name = "applied_date", nullable = false)
	private LocalDate appliedDate;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new credit application that relies on database-managed timestamps.
	 *
	 * @param id unique credit application identifier
	 * @param tenantUnitCredit credit balance being applied
	 * @param invoice invoice receiving the credit
	 * @param amount decimal string applied amount
	 * @param appliedDate date the credit was applied
	 */
	public CreditApplication(
			UUID id,
			TenantUnitCredit tenantUnitCredit,
			Invoice invoice,
			String amount,
			LocalDate appliedDate
	) {
		this.id = id;
		this.tenantUnitCredit = tenantUnitCredit;
		this.invoice = invoice;
		this.amount = amount;
		this.appliedDate = appliedDate;
	}
}
