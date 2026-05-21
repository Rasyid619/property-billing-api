package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.property.PropertyCreateRequest;
import com.propertybilling.dto.property.PropertyIndexResponse;
import com.propertybilling.dto.property.PropertyShowResponse;
import com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
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

	@Nested
	/*
	 * Service tests for creating properties.
	 */
	class CreateProperty {

		@Test
		void createsActiveProperty() {
			ArgumentCaptor<Property> propertyCaptor = ArgumentCaptor.forClass(Property.class);

			propertyService.createProperty(new PropertyCreateRequest("Green Residence", "Bekasi"));

			verify(propertyRepository, times(1)).save(propertyCaptor.capture());
			assertThat(propertyCaptor.getValue().getId()).isNotNull();
			assertThat(propertyCaptor.getValue().getName()).isEqualTo("Green Residence");
			assertThat(propertyCaptor.getValue().getAddress()).isEqualTo("Bekasi");
			assertThat(propertyCaptor.getValue().isActive()).isTrue();
			assertThat(propertyCaptor.getValue().getCreatedAt()).isNotNull();
			assertThat(propertyCaptor.getValue().getUpdatedAt()).isNotNull();
		}
	}

	@Nested
	/*
	 * Service tests for showing one property.
	 */
	class ShowProperty {

		@Test
		void returnsProperty() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(buildEntity(
					"00000000-0000-0000-0000-000000000101",
					"Green Residence",
					"Bekasi",
					true
			)));

			PropertyShowResponse response = propertyService.getProperty(propertyId);

			assertThat(response.id()).isEqualTo(propertyId);
			assertThat(response.name()).isEqualTo("Green Residence");
			assertThat(response.address()).isEqualTo("Bekasi");
			assertThat(response.active()).isTrue();
			verify(propertyRepository, times(1)).findById(propertyId);
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> propertyService.getProperty(propertyId))
					.isInstanceOf(PropertyNotFoundException.class);

			verify(propertyRepository, times(1)).findById(propertyId);
		}
	}

	@Nested
	/*
	 * Service tests for listing properties.
	 */
	class IndexProperties {

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
			verify(propertyRepository, times(1))
					.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void formatsSearchPatternBeforeListingProperties() {
			when(propertyRepository.findIndex(Mockito.eq("%green%"), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			PropertyIndexResponse response = propertyService.listProperties(25, 25, "  green  ", null);

			assertThat(response.count()).isZero();
			assertThat(response.properties()).isEmpty();
			verify(propertyRepository, times(1))
					.findIndex(Mockito.eq("%green%"), Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void treatsBlankSearchAsNoSearch() {
			when(propertyRepository.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			PropertyIndexResponse response = propertyService.listProperties(0, 100, "   ", null);

			assertThat(response.count()).isZero();
			assertThat(response.properties()).isEmpty();
			verify(propertyRepository, times(1))
					.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void filtersByActiveStatus() {
			when(propertyRepository.findIndex(Mockito.isNull(), Mockito.eq(true), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "active");

			assertThat(response.count()).isZero();
			assertThat(response.properties()).isEmpty();
			verify(propertyRepository, times(1))
					.findIndex(Mockito.isNull(), Mockito.eq(true), Mockito.any(Pageable.class));
		}

		@Test
		void filtersByInactiveStatus() {
			when(propertyRepository.findIndex(Mockito.isNull(), Mockito.eq(false), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "inactive");

			assertThat(response.count()).isZero();
			assertThat(response.properties()).isEmpty();
			verify(propertyRepository, times(1))
					.findIndex(Mockito.isNull(), Mockito.eq(false), Mockito.any(Pageable.class));
		}

		@Test
		void passesPageRequestToRepository() {
			when(propertyRepository.findIndex(Mockito.isNull(), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());
			ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

			propertyService.listProperties(200, 100, null, null);

			verify(propertyRepository, times(1))
					.findIndex(Mockito.isNull(), Mockito.isNull(), pageableCaptor.capture());
			assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
			assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
		}

		@Test
		void returnsNoPropertiesForUnsupportedStatus() {
			PropertyIndexResponse response = propertyService.listProperties(0, 100, null, "archived");

			assertThat(response.count()).isZero();
			assertThat(response.properties()).isEmpty();
			verifyNoInteractions(propertyRepository);
		}
	}

	private PropertyIndexQueryResult buildProperty(
			String id,
			String name,
			String address,
			boolean active
	) {
		return new PropertyIndexQueryResult(UUID.fromString(id), name, address, active);
	}

	private Property buildEntity(
			String id,
			String name,
			String address,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-21T09:00:00Z");
		return new Property(
				UUID.fromString(id),
				name,
				address,
				active,
				timestamp,
				timestamp
		);
	}
}
