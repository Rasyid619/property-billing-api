package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.propertybilling.repository.PropertyRepository;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/*
 * Configuration tests for enabling and disabling the invoice automation scheduler.
 */
class InvoiceAutomationSchedulerContextTest {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withUserConfiguration(SchedulerTestConfig.class)
			.withBean(PropertyRepository.class, () -> org.mockito.Mockito.mock(PropertyRepository.class))
			.withBean(InvoiceService.class, () -> org.mockito.Mockito.mock(InvoiceService.class))
			.withBean(Clock.class, Clock::systemUTC)
			.withPropertyValues(
					"app.invoice.automation.cron=0 0 0 25 * *",
					"app.invoice.automation.zone=UTC"
			);

	@Test
	void doesNotCreateSchedulerWhenAutomationIsDisabled() {
		contextRunner
				.withPropertyValues("app.invoice.automation.enabled=false")
				.run(context -> assertThat(context).doesNotHaveBean(InvoiceAutomationScheduler.class));
	}

	@Test
	void createsSchedulerWhenAutomationIsEnabled() {
		contextRunner
				.withPropertyValues("app.invoice.automation.enabled=true")
				.run(context -> assertThat(context).hasSingleBean(InvoiceAutomationScheduler.class));
	}

	@Configuration
	@Import(InvoiceAutomationScheduler.class)
	static class SchedulerTestConfig {
	}
}
