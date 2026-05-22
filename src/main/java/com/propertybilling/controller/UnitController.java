package com.propertybilling.controller;

import com.propertybilling.dto.unit.UnitCreateRequest;
import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.dto.unit.UnitShowResponse;
import com.propertybilling.dto.unit.UnitUpdateRequest;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.UnitService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
/*
 * HTTP boundary for unit endpoints.
 */
public class UnitController {

	private final AuthService authService;
	private final UnitService unitService;

	/**
	 * Creates a unit inside one property for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @param request unit data
	 * @return empty created response
	 */
	@PostMapping("/properties/{property_id}/units")
	ResponseEntity<Void> create(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId,
			@Valid @RequestBody UnitCreateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		unitService.createUnit(propertyId, request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Lists units for one property visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @param offset number of units to skip
	 * @param limit maximum number of units to return
	 * @param status optional active-state filter
	 * @return unit index response
	 */
	@GetMapping("/properties/{property_id}/units")
	ResponseEntity<UnitIndexResponse> indexByProperty(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId,
			@RequestParam(defaultValue = "0") @Min(0) int offset,
			@RequestParam(defaultValue = "100") @Min(1) @Max(100) int limit,
			@RequestParam(required = false) String status
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(unitService.listUnitsByProperty(propertyId, offset, limit, status));
	}

	/**
	 * Gets one unit visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @return unit detail response
	 */
	@GetMapping("/units/{unit_id}")
	ResponseEntity<UnitShowResponse> show(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(unitService.getUnit(unitId));
	}

	/**
	 * Updates one unit for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @param request updated unit data
	 * @return empty no-content response
	 */
	@PutMapping("/units/{unit_id}")
	ResponseEntity<Void> update(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId,
			@Valid @RequestBody UnitUpdateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		unitService.updateUnit(unitId, request);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Deactivates one unit for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @return empty no-content response
	 */
	@DeleteMapping("/units/{unit_id}")
	ResponseEntity<Void> delete(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId
	) {
		authService.authenticateAccessToken(authorizationHeader);
		unitService.deactivateUnit(unitId);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Activates one unit for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @return empty no-content response
	 */
	@PostMapping("/units/{unit_id}/activate")
	ResponseEntity<Void> activate(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId
	) {
		authService.authenticateAccessToken(authorizationHeader);
		unitService.activateUnit(unitId);

		return ResponseEntity.noContent().build();
	}
}
