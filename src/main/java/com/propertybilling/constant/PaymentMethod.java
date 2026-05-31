package com.propertybilling.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Supported payment methods accepted by the public payment API.
 */
public enum PaymentMethod {
	BANK_TRANSFER("bank_transfer"),
	CASH("cash"),
	E_WALLET("e_wallet"),
	OTHER("other");

	private final String value;

	PaymentMethod(String value) {
		this.value = value;
	}

	/**
	 * Returns the public API and database value.
	 *
	 * @return serialized payment method
	 */
	@JsonValue
	public String value() {
		return value;
	}

	/**
	 * Parses public API input into a supported payment method.
	 *
	 * @param value submitted payment method
	 * @return matching payment method
	 */
	@JsonCreator
	public static PaymentMethod fromValue(String value) {
		for (PaymentMethod method : values()) {
			if (method.value.equals(value)) {
				return method;
			}
		}

		throw new IllegalArgumentException("Unsupported payment method");
	}
}
