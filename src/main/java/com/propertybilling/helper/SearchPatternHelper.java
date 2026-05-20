package com.propertybilling.helper;

/*
 * Shared formatting helpers for SQL LIKE search patterns.
 */
public final class SearchPatternHelper {

	private SearchPatternHelper() {
	}

	/**
	 * Formats optional search text as a contains-match LIKE pattern.
	 *
	 * @param search optional search text
	 * @return formatted pattern, or null when search text is blank
	 */
	public static String containsPattern(String search) {
		if (search == null || search.isBlank()) {
			return null;
		}

		return "%" + search.trim() + "%";
	}
}
