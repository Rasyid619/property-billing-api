package com.propertybilling.dto.tenantassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Index element for one tenant assignment history item.
 *
 * @param id unique tenant assignment identifier
 * @param unitId assigned unit identifier
 * @param tenantId assigned tenant identifier
 * @param startDate first date of the tenant assignment
 * @param endDate optional final date of the tenant assignment
 * @param active whether the assignment is currently active
 */
public record TenantAssignmentIndexElement(
		UUID id,
		@JsonProperty("unit_id")
		UUID unitId,
		@JsonProperty("tenant_id")
		UUID tenantId,
		@JsonProperty("start_date")
		LocalDate startDate,
		@JsonProperty("end_date")
		LocalDate endDate,
		boolean active
) {
}
