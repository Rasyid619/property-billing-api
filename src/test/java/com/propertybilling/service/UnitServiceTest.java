package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.unit.UnitCreateRequest;
import com.propertybilling.dto.unit.UnitIndexResponse;
import com.propertybilling.dto.unit.UnitShowResponse;
import com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Unit;
import com.propertybilling.exception.PropertyNotFoundException;
import com.propertybilling.exception.UnitNumberConflictException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.repository.PropertyRepository;
import com.propertybilling.repository.UnitRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;
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
	 * Service tests for creating units.
	 */
	class CreateUnit {

		@Test
		void createsActiveUnit() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			Property property = buildProperty(propertyId);
			ArgumentCaptor<Unit> unitCaptor = ArgumentCaptor.forClass(Unit.class);
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(property));
			when(unitRepository.existsByPropertyIdAndUnitNumber(propertyId, "A-101")).thenReturn(false);

			unitService.createUnit(propertyId, new UnitCreateRequest("A-101", "750000.00", 10));

			verify(propertyRepository, times(1)).findById(propertyId);
			verify(unitRepository, times(1)).existsByPropertyIdAndUnitNumber(propertyId, "A-101");
			verify(unitRepository, times(1)).save(unitCaptor.capture());
			assertThat(unitCaptor.getValue().getId()).isNotNull();
			assertThat(unitCaptor.getValue().getProperty()).isEqualTo(property);
			assertThat(unitCaptor.getValue().getUnitNumber()).isEqualTo("A-101");
			assertThat(unitCaptor.getValue().getMonthlyFee()).isEqualTo("750000.00");
			assertThat(unitCaptor.getValue().getDueDay()).isEqualTo(10);
			assertThat(unitCaptor.getValue().isActive()).isTrue();
			assertThat(unitCaptor.getValue().getCreatedAt()).isNull();
			assertThat(unitCaptor.getValue().getUpdatedAt()).isNull();
		}

		@Test
		void throwsNotFoundWhenPropertyDoesNotExist() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> unitService.createUnit(
					propertyId,
					new UnitCreateRequest("A-101", "750000.00", 10)
			)).isInstanceOf(PropertyNotFoundException.class);

			verify(propertyRepository, times(1)).findById(propertyId);
			verifyNoInteractions(unitRepository);
		}

		@Test
		void throwsConflictWhenUnitNumberExistsInProperty() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(buildProperty(propertyId)));
			when(unitRepository.existsByPropertyIdAndUnitNumber(propertyId, "A-101")).thenReturn(true);

			assertThatThrownBy(() -> unitService.createUnit(
					propertyId,
					new UnitCreateRequest("A-101", "750000.00", 10)
			)).isInstanceOf(UnitNumberConflictException.class);

			verify(propertyRepository, times(1)).findById(propertyId);
			verify(unitRepository, times(1)).existsByPropertyIdAndUnitNumber(propertyId, "A-101");
			verify(unitRepository, Mockito.never()).save(Mockito.any());
		}

		@Test
		void throwsConflictWhenDatabaseUniqueConstraintRejectsUnitNumber() {
			UUID propertyId = UUID.fromString("00000000-0000-0000-0000-000000000101");
			when(propertyRepository.findById(propertyId)).thenReturn(Optional.of(buildProperty(propertyId)));
			when(unitRepository.existsByPropertyIdAndUnitNumber(propertyId, "A-101")).thenReturn(false);
			Mockito.doThrow(new DataIntegrityViolationException("duplicate unit number"))
					.when(unitRepository)
					.save(Mockito.any());

			assertThatThrownBy(() -> unitService.createUnit(
					propertyId,
					new UnitCreateRequest("A-101", "750000.00", 10)
			)).isInstanceOf(UnitNumberConflictException.class);

			verify(propertyRepository, times(1)).findById(propertyId);
			verify(unitRepository, times(1)).existsByPropertyIdAndUnitNumber(propertyId, "A-101");
			verify(unitRepository, times(1)).save(Mockito.any());
		}
	}

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

	@Nested
	/*
	 * Service tests for showing one unit.
	 */
	class ShowUnit {

		@Test
		void returnsUnit() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(unitRepository.findByIdWithProperty(unitId)).thenReturn(Optional.of(buildUnitEntity(
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000101",
					"A-101",
					"750000.00",
					10,
					true
			)));

			UnitShowResponse response = unitService.getUnit(unitId);

			assertThat(response.id()).isEqualTo(unitId);
			assertThat(response.propertyId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000101"));
			assertThat(response.unitNumber()).isEqualTo("A-101");
			assertThat(response.monthlyFee()).isEqualByComparingTo("750000.00");
			assertThat(response.dueDay()).isEqualTo(10);
			assertThat(response.active()).isTrue();
			verify(unitRepository, times(1)).findByIdWithProperty(unitId);
			verifyNoInteractions(propertyRepository);
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.findByIdWithProperty(unitId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> unitService.getUnit(unitId))
					.isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).findByIdWithProperty(unitId);
			verifyNoInteractions(propertyRepository);
		}
	}

	@Nested
	/*
	 * Service tests for deactivating units.
	 */
	class DeleteUnit {

		@Test
		void deactivatesUnitWithWriteLock() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			Unit unit = buildUnitEntity(
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000101",
					"A-101",
					"750000.00",
					10,
					true
			);
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(unit));

			unitService.deactivateUnit(unitId);

			assertThat(unit.isActive()).isFalse();
			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(unitRepository, times(1)).save(unit);
			verifyNoInteractions(propertyRepository);
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> unitService.deactivateUnit(unitId))
					.isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(unitRepository, Mockito.never()).save(Mockito.any());
			verifyNoInteractions(propertyRepository);
		}
	}

	@Nested
	/*
	 * Service tests for activating units.
	 */
	class ActivateUnit {

		@Test
		void activatesUnitWithWriteLock() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			Unit unit = buildUnitEntity(
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000101",
					"A-101",
					"750000.00",
					10,
					false
			);
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(unit));

			unitService.activateUnit(unitId);

			assertThat(unit.isActive()).isTrue();
			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(unitRepository, times(1)).save(unit);
			verifyNoInteractions(propertyRepository);
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> unitService.activateUnit(unitId))
					.isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(unitRepository, Mockito.never()).save(Mockito.any());
			verifyNoInteractions(propertyRepository);
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

	private Property buildProperty(UUID propertyId) {
		return new Property(
				propertyId,
				"Green Residence",
				"Bekasi",
				true
		);
	}

	private Unit buildUnitEntity(
			String id,
			String propertyId,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		return new Unit(
				UUID.fromString(id),
				buildProperty(UUID.fromString(propertyId)),
				unitNumber,
				monthlyFee,
				dueDay,
				active,
				timestamp,
				timestamp
		);
	}
}
