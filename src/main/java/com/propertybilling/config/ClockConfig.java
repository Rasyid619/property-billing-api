package com.propertybilling.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
/*
 * Time source configuration for services that need the current date or instant.
 */
public class ClockConfig {

	@Bean
	Clock clock() {
		return Clock.systemUTC();
	}
}
