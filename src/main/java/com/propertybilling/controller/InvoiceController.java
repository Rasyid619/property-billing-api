package com.propertybilling.controller;

import com.propertybilling.dto.invoice.InvoiceIndexResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.InvoiceService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
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
@RequestMapping("/api/v1")
/*
 * HTTP boundary for invoice endpoints.
 */
public class InvoiceController {

	private final AuthService authService;
	private final InvoiceService invoiceService;

	/**
	 * Lists invoices visible to authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId optional owning property filter
	 * @param unitId optional unit filter
	 * @param tenantId optional tenant filter
	 * @param month optional month filter in YYYY-MM format
	 * @param status optional invoice status filter
	 * @param offset number of invoices to skip
	 * @param limit maximum number of invoices to return
	 * @return invoice index response
	 */
	@GetMapping("/invoices")
	ResponseEntity<InvoiceIndexResponse> index(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam(name = "property_id", required = false) UUID propertyId,
			@RequestParam(name = "unit_id", required = false) UUID unitId,
			@RequestParam(name = "tenant_id", required = false) UUID tenantId,
			@RequestParam(required = false) @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$") String month,
			@RequestParam(required = false) @Pattern(regexp = "unpaid|partial|paid|overdue|cancelled") String status,
			@RequestParam(defaultValue = "0") @Min(0) int offset,
			@RequestParam(defaultValue = "100") @Min(1) @Max(100) int limit
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(invoiceService.listInvoices(
				propertyId,
				unitId,
				tenantId,
				month,
				status,
				offset,
				limit
		));
	}
}
