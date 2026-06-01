package com.propertybilling.controller;

import com.propertybilling.dto.cashbalance.CashBalanceCloseMonthRequest;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.CashBalanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
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
 * HTTP boundary for cash balance endpoints.
 */
public class CashBalanceController {

	private final AuthService authService;
	private final CashBalanceService cashBalanceService;

	/**
	 * Closes a monthly cash balance for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param request close-month request
	 * @return empty created response
	 */
	@PostMapping("/cash-balances/close-month")
	ResponseEntity<Void> closeMonth(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @RequestBody CashBalanceCloseMonthRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		cashBalanceService.closeMonth(request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
}
