package com.propertybilling.dto.common;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.propertybilling.dto.property.PropertyIndexElement;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexElement;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/*
 * Tests shared index response count behavior and resource-specific JSON keys.
 */
class IndexResponseTest {

	private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

	@Test
	void derivesCountFromReturnedItems() {
		PropertyIndexResponse response = new PropertyIndexResponse(List.of(
				new PropertyIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000101"),
						"Green Residence",
						"Bekasi",
						true
				),
				new PropertyIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000102"),
						"Blue Residence",
						"Jakarta",
						false
				)
		));

		assertThat(response.count()).isEqualTo(2);
	}

	@Test
	void keepsTenantAssignmentCollectionKeySnakeCase() throws Exception {
		TenantAssignmentIndexResponse response = new TenantAssignmentIndexResponse(List.of(
				new TenantAssignmentIndexElement(
						UUID.fromString("00000000-0000-0000-0000-000000000201"),
						UUID.fromString("00000000-0000-0000-0000-000000000301"),
						UUID.fromString("00000000-0000-0000-0000-000000000401"),
						LocalDate.parse("2026-01-01"),
						null,
						true
				)
		));

		JsonNode json = objectMapper.readTree(objectMapper.writeValueAsString(response));

		assertThat(json.get("count").asInt()).isEqualTo(1);
		assertThat(json.has("tenant_assignments")).isTrue();
		assertThat(json.has("tenantAssignments")).isFalse();
	}
}
