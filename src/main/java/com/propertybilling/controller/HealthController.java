package com.propertybilling.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/health")
/**
 * HTTP endpoint used to report whether the API is available.
 */
public class HealthController {

	@GetMapping
	ResponseEntity<Void> health() {
		return ResponseEntity.ok().build();
	}
}
