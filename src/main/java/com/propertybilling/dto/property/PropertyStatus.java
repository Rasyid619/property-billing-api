package com.propertybilling.dto.property;

import java.util.Optional;

/*
 * Property status values accepted by property filters.
 */
public enum PropertyStatus {
	ACTIVE(true, "active"),
	INACTIVE(false, "inactive");

	private final boolean active;
	private final String queryValue;

	PropertyStatus(boolean active, String queryValue) {
		this.active = active;
		this.queryValue = queryValue;
	}

	/**
	 * Returns the database active flag represented by this status.
	 *
	 * @return active flag
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Parses a query parameter value into a property status.
	 *
	 * @param value submitted query value
	 * @return matching status, or empty when the value is unsupported
	 */
	public static Optional<PropertyStatus> fromQueryValue(String value) {
		if (value == null || value.isBlank()) {
			return Optional.empty();
		}

		for (PropertyStatus status : values()) {
			if (status.queryValue.equalsIgnoreCase(value.trim())) {
				return Optional.of(status);
			}
		}

		return Optional.empty();
	}
}
