package com.propertybilling.controller;

import com.propertybilling.dto.expense.ExpenseCreateRequest;
import com.propertybilling.dto.expense.ExpenseIndexResponse;
import com.propertybilling.dto.expense.ExpenseUpdateRequest;
import com.propertybilling.service.AuthService;
import com.propertybilling.service.PropertyExpenseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
 * HTTP boundary for property expense endpoints.
 */
public class PropertyExpenseController {

	private final AuthService authService;
	private final PropertyExpenseService propertyExpenseService;

	/**
	 * Creates a property expense for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param request expense creation request
	 * @return empty created response
	 */
	@PostMapping("/expenses")
	ResponseEntity<Void> create(
			@RequestHeader("Authorization") String authorizationHeader,
			@Valid @RequestBody ExpenseCreateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyExpenseService.createExpense(request);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * Lists property expenses for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param propertyId owning property filter
	 * @param month optional month filter in YYYY-MM format
	 * @param offset number of expenses to skip
	 * @param limit maximum number of expenses to return
	 * @return property expense index response
	 */
	@GetMapping("/expenses")
	ResponseEntity<ExpenseIndexResponse> index(
			@RequestHeader("Authorization") String authorizationHeader,
			@RequestParam(name = "property_id") UUID propertyId,
			@RequestParam(required = false) @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$") String month,
			@RequestParam(defaultValue = "0") @Min(0) int offset,
			@RequestParam(defaultValue = "100") @Min(1) @Max(100) int limit
	) {
		authService.authenticateAccessToken(authorizationHeader);

		return ResponseEntity.ok(propertyExpenseService.listExpenses(propertyId, month, offset, limit));
	}

	/**
	 * Replaces a property expense for authenticated admin and staff users.
	 *
	 * @param authorizationHeader bearer access token
	 * @param expenseId property expense identifier
	 * @param request expense replacement request
	 * @return empty no-content response
	 */
	@PutMapping("/expenses/{expense_id}")
	ResponseEntity<Void> update(
			@RequestHeader("Authorization") String authorizationHeader,
			@PathVariable("expense_id") UUID expenseId,
			@Valid @RequestBody ExpenseUpdateRequest request
	) {
		authService.authenticateAccessToken(authorizationHeader);
		propertyExpenseService.updateExpense(expenseId, request);

		return ResponseEntity.noContent().build();
	}
}
