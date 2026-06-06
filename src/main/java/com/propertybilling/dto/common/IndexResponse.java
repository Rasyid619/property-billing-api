package com.propertybilling.dto.common;

import java.util.Collection;

/**
 * Contract for index responses that expose the returned item count.
 */
public interface IndexResponse {

	/**
	 * Returns the number of items included in the response collection.
	 *
	 * @return returned item count
	 */
	int count();

	/**
	 * Counts a response collection using one shared rule.
	 *
	 * @param items returned collection
	 * @return returned item count
	 */
	static int countItems(Collection<?> items) {
		return items.size();
	}
}
