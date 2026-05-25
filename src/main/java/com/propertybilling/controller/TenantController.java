package com.propertybilling.controller;

import com.propertybilling.dto.tenant.TenantIndexResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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
}
