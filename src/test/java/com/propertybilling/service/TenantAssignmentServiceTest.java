package com.propertybilling.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.propertybilling.dto.tenantassignment.TenantAssignmentCreateRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexResponse;
import com.propertybilling.dto.tenantassignment.TenantAssignmentMoveOutRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.entity.Property;
import com.propertybilling.entity.Tenant;
import com.propertybilling.entity.TenantAssignment;
import com.propertybilling.entity.Unit;
import com.propertybilling.exception.TenantAssignmentConflictException;
import com.propertybilling.exception.TenantAssignmentMoveOutDateException;
import com.propertybilling.exception.TenantAssignmentNotFoundException;
import com.propertybilling.exception.TenantNotFoundException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.repository.TenantAssignmentRepository;
import com.propertybilling.repository.TenantRepository;
import com.propertybilling.repository.UnitRepository;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.dao.DataIntegrityViolationException;

/*
 * Unit tests for tenant assignment business workflows.
 */
class TenantAssignmentServiceTest {

	private final UnitRepository unitRepository = Mockito.mock(UnitRepository.class);
	private final TenantRepository tenantRepository = Mockito.mock(TenantRepository.class);
	private final TenantAssignmentRepository tenantAssignmentRepository = Mockito.mock(TenantAssignmentRepository.class);
	private final TenantAssignmentService tenantAssignmentService = new TenantAssignmentService(
			unitRepository,
			tenantRepository,
			tenantAssignmentRepository
	);

	@Nested
	/*
	 * Service tests for creating tenant assignments.
	 */
	class CreateTenantAssignment {

		@Test
		void createsActiveTenantAssignment() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			Unit unit = buildUnit("00000000-0000-0000-0000-000000000201");
			Tenant tenant = buildTenant("00000000-0000-0000-0000-000000000301");
			ArgumentCaptor<TenantAssignment> tenantAssignmentCaptor = ArgumentCaptor.forClass(TenantAssignment.class);
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(unit));
			when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(tenant));
			when(tenantAssignmentRepository.existsActiveByUnitId(unitId)).thenReturn(false);

			tenantAssignmentService.createTenantAssignment(
					unitId,
					new TenantAssignmentCreateRequest(tenantId, LocalDate.parse("2026-05-01"))
			);

			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(tenantRepository, times(1)).findById(tenantId);
			verify(tenantAssignmentRepository, times(1)).existsActiveByUnitId(unitId);
			verify(tenantAssignmentRepository, times(1)).save(tenantAssignmentCaptor.capture());
			assertThat(tenantAssignmentCaptor.getValue().getId()).isNotNull();
			assertThat(tenantAssignmentCaptor.getValue().getUnit()).isEqualTo(unit);
			assertThat(tenantAssignmentCaptor.getValue().getTenant()).isEqualTo(tenant);
			assertThat(tenantAssignmentCaptor.getValue().getStartDate()).isEqualTo(LocalDate.parse("2026-05-01"));
			assertThat(tenantAssignmentCaptor.getValue().getEndDate()).isNull();
			assertThat(tenantAssignmentCaptor.getValue().isActive()).isTrue();
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> tenantAssignmentService.createTenantAssignment(
					unitId,
					new TenantAssignmentCreateRequest(tenantId, LocalDate.parse("2026-05-01"))
			)).isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verifyNoInteractions(tenantRepository, tenantAssignmentRepository);
		}

		@Test
		void throwsNotFoundWhenTenantDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(buildUnit(
					"00000000-0000-0000-0000-000000000201"
			)));
			when(tenantRepository.findById(tenantId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> tenantAssignmentService.createTenantAssignment(
					unitId,
					new TenantAssignmentCreateRequest(tenantId, LocalDate.parse("2026-05-01"))
			)).isInstanceOf(TenantNotFoundException.class);

			verify(unitRepository, times(1)).findByIdForUpdate(unitId);
			verify(tenantRepository, times(1)).findById(tenantId);
			verifyNoInteractions(tenantAssignmentRepository);
		}

		@Test
		void throwsConflictWhenUnitAlreadyHasActiveAssignment() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(buildUnit(
					"00000000-0000-0000-0000-000000000201"
			)));
			when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(buildTenant(
					"00000000-0000-0000-0000-000000000301"
			)));
			when(tenantAssignmentRepository.existsActiveByUnitId(unitId)).thenReturn(true);

			assertThatThrownBy(() -> tenantAssignmentService.createTenantAssignment(
					unitId,
					new TenantAssignmentCreateRequest(tenantId, LocalDate.parse("2026-05-01"))
			)).isInstanceOf(TenantAssignmentConflictException.class);

			verify(tenantAssignmentRepository, times(1)).existsActiveByUnitId(unitId);
			verify(tenantAssignmentRepository, Mockito.never()).save(Mockito.any());
		}

		@Test
		void throwsConflictWhenDatabaseUniqueIndexRejectsActiveAssignment() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			UUID tenantId = UUID.fromString("00000000-0000-0000-0000-000000000301");
			when(unitRepository.findByIdForUpdate(unitId)).thenReturn(Optional.of(buildUnit(
					"00000000-0000-0000-0000-000000000201"
			)));
			when(tenantRepository.findById(tenantId)).thenReturn(Optional.of(buildTenant(
					"00000000-0000-0000-0000-000000000301"
			)));
			when(tenantAssignmentRepository.existsActiveByUnitId(unitId)).thenReturn(false);
			Mockito.doThrow(new DataIntegrityViolationException("duplicate active assignment"))
					.when(tenantAssignmentRepository)
					.save(Mockito.any());

			assertThatThrownBy(() -> tenantAssignmentService.createTenantAssignment(
					unitId,
					new TenantAssignmentCreateRequest(tenantId, LocalDate.parse("2026-05-01"))
			)).isInstanceOf(TenantAssignmentConflictException.class);

			verify(tenantAssignmentRepository, times(1)).save(Mockito.any());
		}
	}

	@Nested
	/*
	 * Service tests for active tenant lookup.
	 */
	class GetActiveTenant {

		@Test
		void returnsActiveTenantAssignment() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			TenantAssignment tenantAssignment = buildTenantAssignment(
					"00000000-0000-0000-0000-000000000401",
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000301",
					true,
					null
			);
			when(unitRepository.existsById(unitId)).thenReturn(true);
			when(tenantAssignmentRepository.findActiveByUnitId(unitId)).thenReturn(Optional.of(tenantAssignment));

			TenantAssignmentShowResponse response = tenantAssignmentService.getActiveTenant(unitId);

			assertThat(response.id()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000401"));
			assertThat(response.unitId()).isEqualTo(unitId);
			assertThat(response.tenantId()).isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000301"));
			assertThat(response.startDate()).isEqualTo(LocalDate.parse("2026-05-01"));
			assertThat(response.endDate()).isNull();
			assertThat(response.active()).isTrue();
			verify(unitRepository, times(1)).existsById(unitId);
			verify(tenantAssignmentRepository, times(1)).findActiveByUnitId(unitId);
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.existsById(unitId)).thenReturn(false);

			assertThatThrownBy(() -> tenantAssignmentService.getActiveTenant(unitId))
					.isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).existsById(unitId);
			verifyNoInteractions(tenantAssignmentRepository);
		}

		@Test
		void throwsNotFoundWhenUnitHasNoActiveTenantAssignment() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(unitRepository.existsById(unitId)).thenReturn(true);
			when(tenantAssignmentRepository.findActiveByUnitId(unitId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> tenantAssignmentService.getActiveTenant(unitId))
					.isInstanceOf(TenantAssignmentNotFoundException.class);

			verify(unitRepository, times(1)).existsById(unitId);
			verify(tenantAssignmentRepository, times(1)).findActiveByUnitId(unitId);
		}
	}

	@Nested
	/*
	 * Service tests for listing tenant assignment history.
	 */
	class ListTenantAssignments {

		@Test
		void listsTenantAssignmentHistory() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(unitRepository.existsById(unitId)).thenReturn(true);
			when(tenantAssignmentRepository.findHistoryByUnitId(unitId)).thenReturn(List.of(
					buildTenantAssignment(
							"00000000-0000-0000-0000-000000000402",
							"00000000-0000-0000-0000-000000000201",
							"00000000-0000-0000-0000-000000000302",
							true,
							null
					),
					buildTenantAssignment(
							"00000000-0000-0000-0000-000000000401",
							"00000000-0000-0000-0000-000000000201",
							"00000000-0000-0000-0000-000000000301",
							false,
							LocalDate.parse("2026-04-30")
					)
			));

			TenantAssignmentIndexResponse response = tenantAssignmentService.listTenantAssignments(unitId);

			assertThat(response.count()).isEqualTo(2);
			assertThat(response.tenantAssignments()).hasSize(2);
			assertThat(response.tenantAssignments().getFirst().id())
					.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000402"));
			assertThat(response.tenantAssignments().getFirst().unitId()).isEqualTo(unitId);
			assertThat(response.tenantAssignments().getFirst().tenantId())
					.isEqualTo(UUID.fromString("00000000-0000-0000-0000-000000000302"));
			assertThat(response.tenantAssignments().getFirst().startDate()).isEqualTo(LocalDate.parse("2026-05-01"));
			assertThat(response.tenantAssignments().getFirst().endDate()).isNull();
			assertThat(response.tenantAssignments().getFirst().active()).isTrue();
			assertThat(response.tenantAssignments().get(1).endDate()).isEqualTo(LocalDate.parse("2026-04-30"));
			assertThat(response.tenantAssignments().get(1).active()).isFalse();
			verify(unitRepository, times(1)).existsById(unitId);
			verify(tenantAssignmentRepository, times(1)).findHistoryByUnitId(unitId);
		}

		@Test
		void returnsEmptyHistoryWhenUnitHasNoAssignments() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000201");
			when(unitRepository.existsById(unitId)).thenReturn(true);
			when(tenantAssignmentRepository.findHistoryByUnitId(unitId)).thenReturn(List.of());

			TenantAssignmentIndexResponse response = tenantAssignmentService.listTenantAssignments(unitId);

			assertThat(response.count()).isZero();
			assertThat(response.tenantAssignments()).isEmpty();
			verify(unitRepository, times(1)).existsById(unitId);
			verify(tenantAssignmentRepository, times(1)).findHistoryByUnitId(unitId);
		}

		@Test
		void throwsNotFoundWhenUnitDoesNotExist() {
			UUID unitId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(unitRepository.existsById(unitId)).thenReturn(false);

			assertThatThrownBy(() -> tenantAssignmentService.listTenantAssignments(unitId))
					.isInstanceOf(UnitNotFoundException.class);

			verify(unitRepository, times(1)).existsById(unitId);
			verify(tenantAssignmentRepository, Mockito.never()).findHistoryByUnitId(Mockito.any());
		}
	}

	@Nested
	/*
	 * Service tests for moving tenants out from active assignments.
	 */
	class MoveOutTenantAssignment {

		@Test
		void closesActiveTenantAssignmentWithWriteLock() {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			TenantAssignment tenantAssignment = buildTenantAssignment(
					"00000000-0000-0000-0000-000000000401",
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000301",
					true,
					null
			);
			when(tenantAssignmentRepository.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(tenantAssignment));

			tenantAssignmentService.moveOutTenantAssignment(
					assignmentId,
					new TenantAssignmentMoveOutRequest(LocalDate.parse("2026-05-31"))
			);

			assertThat(tenantAssignment.getEndDate()).isEqualTo(LocalDate.parse("2026-05-31"));
			assertThat(tenantAssignment.isActive()).isFalse();
			verify(tenantAssignmentRepository, times(1)).findByIdForUpdate(assignmentId);
			verify(tenantAssignmentRepository, times(1)).save(tenantAssignment);
			verifyNoInteractions(unitRepository, tenantRepository);
		}

		@Test
		void throwsNotFoundWhenAssignmentDoesNotExist() {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000999");
			when(tenantAssignmentRepository.findByIdForUpdate(assignmentId)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> tenantAssignmentService.moveOutTenantAssignment(
					assignmentId,
					new TenantAssignmentMoveOutRequest(LocalDate.parse("2026-05-31"))
			)).isInstanceOf(TenantAssignmentNotFoundException.class);

			verify(tenantAssignmentRepository, times(1)).findByIdForUpdate(assignmentId);
			verify(tenantAssignmentRepository, Mockito.never()).save(Mockito.any());
			verifyNoInteractions(unitRepository, tenantRepository);
		}

		@Test
		void throwsNotFoundWhenAssignmentIsAlreadyClosed() {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			when(tenantAssignmentRepository.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(buildTenantAssignment(
					"00000000-0000-0000-0000-000000000401",
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000301",
					false,
					LocalDate.parse("2026-04-30")
			)));

			assertThatThrownBy(() -> tenantAssignmentService.moveOutTenantAssignment(
					assignmentId,
					new TenantAssignmentMoveOutRequest(LocalDate.parse("2026-05-31"))
			)).isInstanceOf(TenantAssignmentNotFoundException.class);

			verify(tenantAssignmentRepository, times(1)).findByIdForUpdate(assignmentId);
			verify(tenantAssignmentRepository, Mockito.never()).save(Mockito.any());
		}

		@Test
		void throwsBadRequestWhenEndDateIsBeforeStartDate() {
			UUID assignmentId = UUID.fromString("00000000-0000-0000-0000-000000000401");
			TenantAssignment tenantAssignment = buildTenantAssignment(
					"00000000-0000-0000-0000-000000000401",
					"00000000-0000-0000-0000-000000000201",
					"00000000-0000-0000-0000-000000000301",
					true,
					null
			);
			when(tenantAssignmentRepository.findByIdForUpdate(assignmentId)).thenReturn(Optional.of(tenantAssignment));

			assertThatThrownBy(() -> tenantAssignmentService.moveOutTenantAssignment(
					assignmentId,
					new TenantAssignmentMoveOutRequest(LocalDate.parse("2026-04-30"))
			)).isInstanceOf(TenantAssignmentMoveOutDateException.class);

			assertThat(tenantAssignment.getEndDate()).isNull();
			assertThat(tenantAssignment.isActive()).isTrue();
			verify(tenantAssignmentRepository, times(1)).findByIdForUpdate(assignmentId);
			verify(tenantAssignmentRepository, Mockito.never()).save(Mockito.any());
		}
	}

	private TenantAssignment buildTenantAssignment(
			String id,
			String unitId,
			String tenantId,
			boolean active,
			LocalDate endDate
	) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");

		return new TenantAssignment(
				UUID.fromString(id),
				buildUnit(unitId),
				buildTenant(tenantId),
				LocalDate.parse("2026-05-01"),
				endDate,
				active,
				timestamp,
				timestamp
		);
	}

	private Unit buildUnit(String unitId) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");
		Property property = new Property(
				UUID.fromString("00000000-0000-0000-0000-000000000101"),
				"Green Residence",
				"Bekasi",
				true,
				timestamp,
				timestamp
		);

		return new Unit(
				UUID.fromString(unitId),
				property,
				"A-101",
				"750000.00",
				10,
				true,
				timestamp,
				timestamp
		);
	}

	private Tenant buildTenant(String tenantId) {
		OffsetDateTime timestamp = OffsetDateTime.parse("2026-05-22T09:00:00Z");

		return new Tenant(
				UUID.fromString(tenantId),
				"Budi",
				"08123456789",
				"budi@example.com",
				timestamp,
				timestamp
		);
	}
}
