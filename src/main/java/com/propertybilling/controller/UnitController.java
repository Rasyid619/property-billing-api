package com.propertybilling.controller;

import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.UnitService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/properties/{property_id}/units")
/*
 * HTTP boundary for property unit endpoints.
 */
public class UnitController {

	private final AuthService authService;
	private final UnitService unitService;

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
	@GetMapping
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
}
