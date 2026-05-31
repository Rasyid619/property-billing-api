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
@Table(name = "property_expenses")
/*
 * Persisted money spent for one property.
 */
public class PropertyExpense {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;

	@Column(name = "expense_date", nullable = false)
	private LocalDate expenseDate;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private String amount;

	private String description;

	@Column(name = "receipt_url")
	private String receiptUrl;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new property expense that relies on database-managed timestamps.
	 *
	 * @param id unique expense identifier
	 * @param property property that owns the expense
	 * @param expenseDate date the expense happened
	 * @param category expense category
	 * @param amount decimal string expense amount
	 * @param description optional expense description
	 * @param receiptUrl optional receipt URL
	 */
	public PropertyExpense(
			UUID id,
			Property property,
			LocalDate expenseDate,
			String category,
			String amount,
			String description,
			String receiptUrl
	) {
		this(id, property, expenseDate, category, amount, description, receiptUrl, null, null);
	}

	/**
	 * Creates a persisted property expense representation.
	 *
	 * @param id unique expense identifier
	 * @param property property that owns the expense
	 * @param expenseDate date the expense happened
	 * @param category expense category
	 * @param amount decimal string expense amount
	 * @param description optional expense description
	 * @param receiptUrl optional receipt URL
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public PropertyExpense(
			UUID id,
			Property property,
			LocalDate expenseDate,
			String category,
			String amount,
			String description,
			String receiptUrl,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.property = property;
		this.expenseDate = expenseDate;
		this.category = category;
		this.amount = amount;
		this.description = description;
		this.receiptUrl = receiptUrl;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
