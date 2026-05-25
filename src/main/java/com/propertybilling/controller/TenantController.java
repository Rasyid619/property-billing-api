package com.propertybilling.controller;

import com.propertybilling.dto.tenant.TenantCreateRequest;
import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.dto.tenant.TenantShowResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/tenants")
/*
 * HTTP boundary for tenant endpoints.
 */
public class TenantController {

	private final AuthService authService;
	private final TenantService tenantService;

	/**
	 * Lists tenant data records visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param offset number of tenants to skip
	 * @param limit maximum number of tenants to return
	 * @param search optional text filter
	 * @return tenant index response
	 */
	@GetMapping
	ResponseEntity<TenantIndexResponse> index(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam(defaultValue = "0") @Min(0) int offset,
			@RequestParam(defaultValue = "100") @Min(1) @Max(100) int limit,
			@RequestParam(required = false) @Size(max = 100) String search
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(tenantService.listTenants(offset, limit, search));
	}

	/**
	 * Creates a tenant data record for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param request tenant data
	 * @return empty created response
	 */
	@PostMapping
	ResponseEntity<Void> create(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @RequestBody TenantCreateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		tenantService.createTenant(request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Gets one tenant data record visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param tenantId tenant identifier
	 * @return tenant detail response
	 */
	@GetMapping("/{tenant_id}")
	ResponseEntity<TenantShowResponse> show(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("tenant_id") UUID tenantId
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(tenantService.getTenant(tenantId));
	}
}
