package com.propertybilling.helper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/*
 * Unit tests for reusable search pattern formatting.
 */
class SearchPatternHelperTest {

	@Test
	void returnsNullWhenSearchIsNull() {
		assertThat(SearchPatternHelper.containsPattern(null)).isNull();
	}

	@Test
	void returnsNullWhenSearchIsBlank() {
		assertThat(SearchPatternHelper.containsPattern("   ")).isNull();
	}

	@Test
	void formatsTrimmedContainsPattern() {
		assertThat(SearchPatternHelper.containsPattern("  green  ")).isEqualTo("%green%");
	}
}
