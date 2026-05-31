package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tenant_unit_credits")
/*
 * Reusable credit balance for one tenant and unit.
 */
public class TenantUnitCredit {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private Unit unit;

	@Column(name = "balance", nullable = false)
	private String balance;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new credit balance that relies on database-managed timestamps.
	 *
	 * @param id unique credit identifier
	 * @param tenant tenant that owns the credit
	 * @param unit unit that owns the credit
	 * @param balance decimal string credit balance
	 */
	public TenantUnitCredit(
			UUID id,
			Tenant tenant,
			Unit unit,
			String balance
	) {
		this.id = id;
		this.tenant = tenant;
		this.unit = unit;
		this.balance = balance;
	}

	/**
	 * Increases the reusable credit balance.
	 *
	 * @param amount amount to add
	 */
	public void increaseBalance(BigDecimal amount) {
		balance = new BigDecimal(balance).add(amount).toPlainString();
	}

	/**
	 * Decreases the reusable credit balance.
	 *
	 * @param amount amount to subtract
	 */
	public void decreaseBalance(BigDecimal amount) {
		balance = new BigDecimal(balance).subtract(amount).toPlainString();
	}
}
