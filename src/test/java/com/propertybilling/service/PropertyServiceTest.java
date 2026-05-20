package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult;
import com.propertybilling.repository.PropertyRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

/*
 * Unit tests for property business workflows.
 */
class PropertyServiceTest {

	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final PropertyService propertyService = new PropertyService(propertyRepository);

	@Test
	void listsProperties() {
		PropertyIndexQueryResult property = buildProperty(
				"00000000-0000-0000-0000-000000000101",
				"Green Residence",
				"Bekasi",
				true
		);
		when(propertyRepository.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class)))
				.thenReturn(List.of(property));

		PropertyIndexResponse response = propertyService.listProperties(0, 100, null, null);

		assertThat(response.count()).isEqualTo(1);
		assertThat(response.properties()).hasSize(1);
		assertThat(response.properties().getFirst().id())
				.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000101"));
		assertThat(response.properties().getFirst().name()).isEqualTo("Green Residence");
		assertThat(response.properties().getFirst().address()).isEqualTo("Bekasi");
		assertThat(response.properties().getFirst().active()).isTrue();
	}

	@Test
	void formatsSearchPatternBeforeListingProperties() {
		when(propertyRepository.findIndex(Mockito.eq("%green%"), Mockito.isNull(), Mockito.any(Pageable.class)))
				.thenReturn(List.of());

		PropertyIndexResponse response = propertyService.listProperties(25, 25, "  green  ", null);

		assertThat(response.count()).isZero();
		assertThat(response.properties()).isEmpty();
		verify(propertyRepository).findIndex(Mockito.eq("%green%"), Mockito.isNull(), Mockito.any(Pageable.class));
	}

	@Test
	void treatsBlankSearchAsNoSearch() {
		when(propertyRepository.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class)))
				.thenReturn(List.of());

		PropertyIndexResponse response = propertyService.listProperties(0, 100, "   ", null);

		assertThat(response.count()).isZero();
		assertThat(response.properties()).isEmpty();
	}

	@Test
	void filtersByActiveStatus() {
		when(propertyRepository.findIndex(Mockito.isNull(), Mockito.eq(true), Mockito.any(Pageable.class)))
				.thenReturn(List.of());

		PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "active");

		assertThat(response.count()).isZero();
		assertThat(response.properties()).isEmpty();
	}

	@Test
	void filtersByInactiveStatus() {
		when(propertyRepository.findIndex(Mockito.isNull(), Mockito.eq(false), Mockito.any(Pageable.class)))
				.thenReturn(List.of());

		PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "inactive");

		assertThat(response.count()).isZero();
		assertThat(response.properties()).isEmpty();
	}

	@Test
	void passesPageRequestToRepository() {
		when(propertyRepository.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class)))
				.thenReturn(List.of());
		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

		propertyService.listProperties(200, 100, null, null);

		verify(propertyRepository).findIndex(Mockito.isNull(), Mockito.isNull(), pageableCaptor.capture());
		assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
		assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
	}

	@Test
	void returnsNoPropertiesForUnsupportedStatus() {
		PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "archived");

		assertThat(response.count()).isZero();
		assertThat(response.properties()).isEmpty();
	}

	private PropertyIndexQueryResult buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		return new PropertyIndexQueryResult(UUID.fromString(id), name, address, active);
	}
}
