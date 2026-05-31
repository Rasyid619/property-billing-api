package com.propertybilling.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.List;

/*
 * Supported invoice statuses used by persistence and business workflows.
 */
public enum InvoiceStatus {
	UNPAID("unpaid"),
	PARTIAL("partial"),
	PAID("paid"),
	OVERDUE("overdue"),
	CANCELLED("cancelled");

	private final String value;

	InvoiceStatus(String value) {
		this.value = value;
	}

	/**
	 * Returns the public API and database value.
	 *
	 * @return serialized invoice status
	 */
	@JsonValue
	public String value() {
		return value;
	}

	/**
	 * Parses a public API or database value into an invoice status.
	 *
	 * @param value submitted invoice status
	 * @return matching invoice status
	 */
	@JsonCreator
	public static InvoiceStatus fromValue(String value) {
		for (InvoiceStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}

		throw new IllegalArgumentException("Unsupported invoice status");
	}

	/**
	 * Returns statuses that can still receive payments.
	 *
	 * @return open invoice statuses
	 */
	public static List<InvoiceStatus> openStatuses() {
		return List.of(
				UNPAID,
				PARTIAL,
				OVERDUE
		);
	}
}
