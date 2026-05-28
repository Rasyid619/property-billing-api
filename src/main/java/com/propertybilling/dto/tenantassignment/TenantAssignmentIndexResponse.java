package com.propertybilling.dto.tenantassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
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
) {
}
