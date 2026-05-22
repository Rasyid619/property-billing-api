package com.propertybilling.dto.common;

import java.util.Optional;

/*
 * Reusable active-state filter accepted by list endpoints.
 */
public enum StatusFilter {
	ACTIVE(true, "active"),
	INACTIVE(false, "inactive");

	private final boolean active;
	private final String queryValue;

	StatusFilter(boolean active, String queryValue) {
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
	 * Parses a query parameter value into a reusable active-state filter.
	 *
	 * @param value submitted query value
	 * @return matching filter, or empty when the value is unsupported or unset
	 */
	public static Optional<StatusFilter> fromQueryValue(String value) {
		if (isUnset(value)) {
			return Optional.empty();
		}

		for (StatusFilter status : values()) {
			if (status.queryValue.equalsIgnoreCase(value.trim())) {
				return Optional.of(status);
			}
		}

		return Optional.empty();
	}

	/**
	 * Checks whether the query value should be treated as no filter.
	 *
	 * @param value submitted query value
	 * @return true when no filter was submitted
	 */
	public static boolean isUnset(String value) {
		return value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim());
	}
}
