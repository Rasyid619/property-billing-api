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
@Table(name = "cash_balances")
/*
 * Persisted monthly cash closing balance for one property.
 */
public class CashBalance {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;

	@Column(name = "month", nullable = false)
	private LocalDate month;

	@Column(name = "opening_balance", nullable = false)
	private String openingBalance;

	@Column(name = "total_income", nullable = false)
	private String totalIncome;

	@Column(name = "total_expense", nullable = false)
	private String totalExpense;

	@Column(name = "closing_balance", nullable = false)
	private String closingBalance;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new monthly cash balance that relies on database-managed timestamps.
	 *
	 * @param id unique balance identifier
	 * @param property owning property
	 * @param month first day of the closed month
	 * @param openingBalance decimal string opening balance
	 * @param totalIncome decimal string total cash income
	 * @param totalExpense decimal string total expense
	 * @param closingBalance decimal string closing balance
	 */
	public CashBalance(
			UUID id,
			Property property,
			LocalDate month,
			String openingBalance,
			String totalIncome,
			String totalExpense,
			String closingBalance
	) {
		this.id = id;
		this.property = property;
		this.month = month;
		this.openingBalance = openingBalance;
		this.totalIncome = totalIncome;
		this.totalExpense = totalExpense;
		this.closingBalance = closingBalance;
	}
}
