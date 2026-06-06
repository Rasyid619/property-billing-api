package com.propertybilling.service;

import com.propertybilling.dto.tenantassignment.TenantAssignmentCreateRequest;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexElement;
import com.propertybilling.dto.tenantassignment.TenantAssignmentIndexResponse;
import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
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
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for tenant assignments.
 */
public class TenantAssignmentService {

	private final UnitRepository unitRepository;
	private final TenantRepository tenantRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;

	/**
	 * Creates an active tenant assignment for one unit.
	 *
	 * @param unitId unit identifier
	 * @param request tenant assignment data
	 * @throws UnitNotFoundException when no unit exists for the ID
	 * @throws TenantNotFoundException when no tenant exists for the ID
	 * @throws TenantAssignmentConflictException when the unit already has an active tenant assignment
	 */
	@Transactional
	public void createTenantAssignment(UUID unitId, TenantAssignmentCreateRequest request) {
		Unit unit = unitRepository.findByIdForUpdate(unitId)
				.orElseThrow(UnitNotFoundException::new);
		Tenant tenant = tenantRepository.findById(request.tenantId())
				.orElseThrow(TenantNotFoundException::new);

		if (tenantAssignmentRepository.existsActiveByUnitId(unitId)) {
			throw new TenantAssignmentConflictException();
		}

		try {
			tenantAssignmentRepository.save(new TenantAssignment(
					UUID.randomUUID(),
					unit,
					tenant,
					request.startDate(),
					null,
					true
			));
		} catch (DataIntegrityViolationException exception) {
			throw new TenantAssignmentConflictException();
		}
	}

	/**
	 * Returns the active tenant assignment for one unit.
	 *
	 * @param unitId unit identifier
	 * @return active tenant assignment response
	 * @throws UnitNotFoundException when no unit exists for the ID
	 * @throws TenantAssignmentNotFoundException when the unit has no active tenant assignment
	 */
	@Transactional(readOnly = true)
	public TenantAssignmentShowResponse getActiveTenant(UUID unitId) {
		if (!unitRepository.existsById(unitId)) {
			throw new UnitNotFoundException();
		}

		return tenantAssignmentRepository.findActiveByUnitId(unitId)
				.map(this::toShowResponse)
				.orElseThrow(TenantAssignmentNotFoundException::new);
	}

	/**
	 * Lists tenant assignment history for one unit.
	 *
	 * @param unitId unit identifier
	 * @return tenant assignment index response
	 * @throws UnitNotFoundException when no unit exists for the ID
	 */
	@Transactional(readOnly = true)
	public TenantAssignmentIndexResponse listTenantAssignments(UUID unitId) {
		if (!unitRepository.existsById(unitId)) {
			throw new UnitNotFoundException();
		}

		List<TenantAssignmentIndexElement> tenantAssignments = tenantAssignmentRepository.findHistoryByUnitId(unitId)
				.stream()
				.map(this::toIndexElement)
				.toList();

		return new TenantAssignmentIndexResponse(tenantAssignments);
	}

	/**
	 * Closes an active tenant assignment.
	 *
	 * @param assignmentId assignment identifier
	 * @throws TenantAssignmentNotFoundException when no active assignment exists for the ID
	 * @throws TenantAssignmentMoveOutDateException when today's date is before the assignment start date
	 */
	@Transactional
	public void moveOutTenantAssignment(UUID assignmentId) {
		TenantAssignment tenantAssignment = tenantAssignmentRepository.findByIdForUpdate(assignmentId)
				.orElseThrow(TenantAssignmentNotFoundException::new);

		if (!tenantAssignment.isActive() || tenantAssignment.getEndDate() != null) {
			throw new TenantAssignmentNotFoundException();
		}

		LocalDate endDate = LocalDate.now();

		if (endDate.isBefore(tenantAssignment.getStartDate())) {
			throw new TenantAssignmentMoveOutDateException();
		}

		tenantAssignment.moveOut(endDate);
		tenantAssignmentRepository.save(tenantAssignment);
	}

	private TenantAssignmentIndexElement toIndexElement(TenantAssignment tenantAssignment) {
		return new TenantAssignmentIndexElement(
				tenantAssignment.getId(),
				tenantAssignment.getUnit().getId(),
				tenantAssignment.getTenant().getId(),
				tenantAssignment.getStartDate(),
				tenantAssignment.getEndDate(),
				tenantAssignment.isActive()
		);
	}

	private TenantAssignmentShowResponse toShowResponse(TenantAssignment tenantAssignment) {
		return new TenantAssignmentShowResponse(
				tenantAssignment.getId(),
				tenantAssignment.getUnit().getId(),
				tenantAssignment.getTenant().getId(),
				tenantAssignment.getStartDate(),
				tenantAssignment.getEndDate(),
				tenantAssignment.isActive()
		);
	}
}
