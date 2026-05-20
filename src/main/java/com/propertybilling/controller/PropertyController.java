package com.propertybilling.controller;

import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/properties")
/*
 * HTTP boundary for property management endpoints.
 */
public class PropertyController {

	private final AuthService authService;
	private final PropertyService propertyService;

	/**
	 * Creates the property controller.
	 *
	 * @param authService access-token validation workflow
	 * @param propertyService property business workflow
	 */
	public PropertyController(
			AuthService authService,
			PropertyService propertyService
	) {
		this.authService = authService;
		this.propertyService = propertyService;
	}

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
}
