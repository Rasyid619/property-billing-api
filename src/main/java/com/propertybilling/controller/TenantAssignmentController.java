package com.propertybilling.controller;

import com.propertybilling.dto.tenantassignment.TenantAssignmentCreateRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexResponse;
import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.TenantAssignmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
	 * Assigns a tenant to a unit for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @param request assignment data
	 * @return empty created response
	 */
	@PostMapping("/units/{unit_id}/tenant-assignments")
	ResponseEntity<Void> create(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId,
			@Valid @RequestBody TenantAssignmentCreateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		tenantAssignmentService.createTenantAssignment(unitId, request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

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

	/**
	 * Lists tenant assignment history for a unit visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param unitId unit identifier
	 * @return tenant assignment history response
	 */
	@GetMapping("/units/{unit_id}/tenant-assignments")
	ResponseEntity<TenantAssignmentIndexResponse> indexByUnit(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("unit_id") UUID unitId
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(tenantAssignmentService.listTenantAssignments(unitId));
	}

	/**
	 * Moves a tenant out from an active assignment for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param assignmentId assignment identifier
	 * @return empty no-content response
	 */
	@PatchMapping("/unit-tenant-assignments/{assignmentId}/move-out")
	ResponseEntity<Void> moveOut(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable UUID assignmentId
	) {
		authService.authenticateAccessToken(authorizationHeader);
		tenantAssignmentService.moveOutTenantAssignment(assignmentId);

		return ResponseEntity.noContent().build();
	}
}
