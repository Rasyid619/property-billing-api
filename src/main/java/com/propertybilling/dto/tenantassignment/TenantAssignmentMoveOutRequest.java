package com.propertybilling.dto.tenantassignment;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Request body for closing an active tenant assignment.
 *
 * @param endDate final date of the tenant assignment
 */
public record TenantAssignmentMoveOutRequest(
		@NotNull
		@JsonProperty("end_date")
		LocalDate endDate
) {
}
