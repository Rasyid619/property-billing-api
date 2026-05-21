package com.propertybilling.controller;

import com.propertybilling.dto.property.PropertyCreateRequest;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.PropertyShowResponse;
import com.propertybilling.dto.property.PropertyUpdateRequest;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/properties")
/*
 * HTTP boundary for property management endpoints.
 */
public class PropertyController {

	private final AuthService authService;
	private final PropertyService propertyService;

	/**
	 * Lists properties visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param offset number of properties to skip
	 * @param limit maximum number of properties to return
	 * @param search optional text filter
	 * @param status optional active-state filter
	 * @return property index response
	 */
	@GetMapping
	ResponseEntity<PropertyIndexResponse> index(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam(defaultValue = "0") int offset,
			@RequestParam(defaultValue = "100") int limit,
			@RequestParam(required = false) String search,
			@RequestParam(required = false) String status
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(propertyService.listProperties(offset, limit, search, status));
	}

	/**
	 * Deactivates one property for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @return empty no-content response
	 */
	@DeleteMapping("/{property_id}")
	ResponseEntity<Void> delete(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyService.deactivateProperty(propertyId);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Activates one property for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @return empty no-content response
	 */
	@PostMapping("/{property_id}/activate")
	ResponseEntity<Void> activate(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyService.activateProperty(propertyId);

		return ResponseEntity.noContent().build();
	}

	/**
	 * Gets one property visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @return property detail response
	 */
	@GetMapping("/{property_id}")
	ResponseEntity<PropertyShowResponse> show(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(propertyService.getProperty(propertyId));
	}

	/**
	 * Creates a property for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param request property data
	 * @return empty created response
	 */
	@PostMapping
	ResponseEntity<Void> create(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @RequestBody PropertyCreateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyService.createProperty(request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Updates a property for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId property identifier
	 * @param request updated property data
	 * @return empty no-content response
	 */
	@PutMapping("/{property_id}")
	ResponseEntity<Void> update(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("property_id") UUID propertyId,
			@Valid @RequestBody PropertyUpdateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyService.updateProperty(propertyId, request);

		return ResponseEntity.noContent().build();
	}
}
