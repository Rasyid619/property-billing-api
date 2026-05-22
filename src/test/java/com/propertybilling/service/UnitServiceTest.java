package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UnitRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.data.domain.Pageable;

/*
 * Unit tests for unit business workflows.
 */
class UnitServiceTest {

	private final PropertyRepository propertyRepository = Mockito.mock(PropertyRepository.class);
	private final UnitRepository unitRepository = Mockito.mock(UnitRepository.class);
	private final UnitService unitService = new UnitService(propertyRepository, unitRepository);

	@Nested
	/*
	 * Service tests for listing units by property.
	 */
	class IndexUnitsByProperty {

		@Test
		void listsUnitsByProperty() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.existsById(propertyId)).thenReturn(true);
			when(unitRepository.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of(buildUnit(
							"00000000-0000-0000-0000-000000000201",
							"00000000-0000-0000-0000-000000000101",
							"A-101",
							"750000.00",
							10,
							true
					)));

			UnitIndexResponse response = unitService.listUnitsByProperty(propertyId, 0, 100, null);

			assertThat(response.count()).isEqualTo(1);
			assertThat(response.units()).hasSize(1);
			assertThat(response.units().getFirst().id())
					.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000201"));
			assertThat(response.units().getFirst().propertyId()).isEqualTo(propertyId);
			assertThat(response.units().getFirst().unitNumber()).isEqualTo("A-101");
			assertThat(response.units().getFirst().monthlyFee()).isEqualByComparingTo("750000.00");
			assertThat(response.units().getFirst().dueDay()).isEqualTo(10);
			assertThat(response.units().getFirst().active()).isTrue();
			verify(propertyRepository, times(1)).existsById(propertyId);
			verify(unitRepository, times(1))
					.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void filtersByActiveStatus() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.existsById(propertyId)).thenReturn(true);
			when(unitRepository.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.eq(true), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			UnitIndexResponse response = unitService.listUnitsByProperty(propertyId, 0, 100, "active");

			assertThat(response.count()).isZero();
			assertThat(response.units()).isEmpty();
			verify(unitRepository, times(1))
					.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.eq(true), Mockito.any(Pageable.class));
		}

		@Test
		void treatsLiteralNullStatusAsNoFilter() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.existsById(propertyId)).thenReturn(true);
			when(unitRepository.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			UnitIndexResponse response = unitService.listUnitsByProperty(propertyId, 0, 100, "null");

			assertThat(response.count()).isZero();
			assertThat(response.units()).isEmpty();
			verify(unitRepository, times(1))
					.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), Mockito.any(Pageable.class));
		}

		@Test
		void returnsNoUnitsForUnsupportedStatus() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.existsById(propertyId)).thenReturn(true);

			UnitIndexResponse response = unitService.listUnitsByProperty(propertyId, 0, 100, "archived");

			assertThat(response.count()).isZero();
			assertThat(response.units()).isEmpty();
			verifyNoInteractions(unitRepository);
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(propertyRepository.existsById(propertyId)).thenReturn(false);

			assertThatThrownBy(() -> unitService.listUnitsByProperty(propertyId, 0, 100, null))
					.isInstanceOf(PropertyNotFoundException.class);

			verify(propertyRepository, times(1)).existsById(propertyId);
			verifyNoInteractions(unitRepository);
		}

		@Test
		void passesPageRequestToRepository() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
			when(propertyRepository.existsById(propertyId)).thenReturn(true);
			when(unitRepository.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), Mockito.any(Pageable.class)))
					.thenReturn(List.of());

			unitService.listUnitsByProperty(propertyId, 200, 100, null);

			verify(unitRepository, times(1))
					.findIndexByPropertyId(Mockito.eq(propertyId), Mockito.isNull(), pageableCaptor.capture());
			assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
			assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(100);
		}
	}

	private UnitIndexQueryResult buildUnit(
			String id,
			String propertyId,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		return new UnitIndexQueryResult(
				UUID.fromString(id),
				UUID.fromString(propertyId),
				unitNumber,
				monthlyFee,
				dueDay,
				active
		);
	}
}
