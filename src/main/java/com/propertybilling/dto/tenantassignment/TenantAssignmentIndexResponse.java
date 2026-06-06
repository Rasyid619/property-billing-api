package com.propertybilling.dto.tenantassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.propertybilling.dto.common.IndexResponse;
import java.util.List;

/**
 * Index response for tenant assignment history.
 *
 * @param count number of tenant assignments returned
 * @param tenantAssignments tenant assignment history items
 */
public record TenantAssignmentIndexResponse(
		int count,
		@JsonProperty("tenant_assignments")
		List<TenantAssignmentIndexElement> tenantAssignments
) implements IndexResponse {

	/**
	 * Builds a response with count derived from the returned tenant assignment items.
	 *
	 * @param tenantAssignments tenant assignment history items
	 */
	public TenantAssignmentIndexResponse(List<TenantAssignmentIndexElement> tenantAssignments) {
		this(IndexResponse.countItems(tenantAssignments), tenantAssignments);
	}
}
