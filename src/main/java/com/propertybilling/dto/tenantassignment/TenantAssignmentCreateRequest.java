package com.propertybilling.dto.tenantassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request body for assigning a tenant to one unit.
 *
 * @param tenantId tenant identifier to assign
 * @param startDate first date of the assignment
 */
public record TenantAssignmentCreateRequest(
		@NotNull
		@JsonProperty("tenant_id")
		UUID tenantId,
		@NotNull
		@JsonProperty("start_date")
		LocalDate startDate
) {
}
