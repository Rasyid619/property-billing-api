package com.propertybilling.controller;

import com.propertybilling.dto.report.CashFlowReportResponse;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.ReportService;
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
 * HTTP boundary for reporting endpoints.
 */
public class ReportController {

	private final AuthService authService;
	private final ReportService reportService;

	/**
	 * Gets a cash-flow report for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId owning property identifier
	 * @param month report month in YYYY-MM format
	 * @return cash-flow report response
	 */
	@GetMapping("/reports/cash-flow")
	ResponseEntity<CashFlowReportResponse> cashFlow(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam(name = "property_id") UUID propertyId,
			@RequestParam @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$") String month
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(reportService.getCashFlowReport(propertyId, month));
	}
}
