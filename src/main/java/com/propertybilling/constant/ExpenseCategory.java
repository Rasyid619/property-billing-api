package com.propertybilling.constant;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/*
 * Supported property expense categories accepted by the public expense API.
 */
public enum ExpenseCategory {
	ELECTRICITY("electricity"),
	WATER("water"),
	SECURITY("security"),
	CLEANING("cleaning"),
	SOCIAL_HELP("social_help"),
	REPAIR("repair"),
	MAINTENANCE("maintenance"),
	OTHER("other");

	private final String value;

	ExpenseCategory(String value) {
		this.value = value;
	}

	/**
	 * Returns the public API and database value.
	 *
	 * @return serialized expense category
	 */
	@JsonValue
	public String value() {
		return value;
	}

	/**
	 * Parses public API input into a supported expense category.
	 *
	 * @param value submitted expense category
	 * @return matching expense category
	 */
	@JsonCreator
	public static ExpenseCategory fromValue(String value) {
		for (ExpenseCategory category : values()) {
			if (category.value.equals(value)) {
				return category;
			}
		}

		throw new IllegalArgumentException("Unsupported expense category");
	}
}
