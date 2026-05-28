package com.propertybilling.controller;

import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantAssignmentService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
/*
 * HTTP boundary for tenant assignment endpoints.
 */
public class TenantAssignmentController {

	private final AuthService authService;
	private final TenantAssignmentService tenantAssignmentService;

	/**
	 * Gets the active tenant assignment for a unit visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @return active tenant assignment response
	 */
	@GetMapping("/units/{unit_id}/active-tenant")
	ResponseEntity<TenantAssignmentShowResponse> showActiveTenant(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(tenantAssignmentService.getActiveTenant(unitId));
	}
}
