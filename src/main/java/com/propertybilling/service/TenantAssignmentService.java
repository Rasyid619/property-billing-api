package com.propertybilling.service;

import com.propertybilling.dto.tenantassignment.TenantAssignmentShowResponse;
import com.propertybilling.entity.TenantAssignment;
import com.propertybilling.exception.TenantAssignmentNotFoundException;
import com.propertybilling.exception.UnitNotFoundException;
import com.propertybilling.repository.TenantAssignmentRepository;
import com.propertybilling.repository.UnitRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/*
 * Business workflow for tenant assignment lookups.
 */
public class TenantAssignmentService {

	private final UnitRepository unitRepository;
	private final TenantAssignmentRepository tenantAssignmentRepository;

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
