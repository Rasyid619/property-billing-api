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
@Table(name = "payments")
/*
 * Persisted payment applied to an invoice.
 */
public class Payment {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "invoice_id", nullable = false)
	private Invoice invoice;

	@Column(name = "amount", nullable = false)
	private String amount;

	@Column(name = "payment_date", nullable = false)
	private LocalDate paymentDate;

	@Column(name = "payment_method", nullable = false)
	private String paymentMethod;

	@Column(name = "reference_number")
	private String referenceNumber;

	@Column(name = "note")
	private String note;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new payment that relies on database-managed timestamps.
	 *
	 * @param id unique payment identifier
	 * @param invoice invoice receiving the payment
	 * @param amount decimal string payment amount
	 * @param paymentDate date the payment was made
	 * @param paymentMethod payment method label
	 * @param referenceNumber optional external reference number
	 * @param note optional payment note
	 */
	public Payment(
			UUID id,
			Invoice invoice,
			String amount,
			LocalDate paymentDate,
			String paymentMethod,
			String referenceNumber,
			String note
	) {
		this.id = id;
		this.invoice = invoice;
		this.amount = amount;
		this.paymentDate = paymentDate;
		this.paymentMethod = paymentMethod;
		this.referenceNumber = referenceNumber;
		this.note = note;
	}
}
