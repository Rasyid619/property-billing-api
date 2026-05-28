package com.propertybilling.repository;

import com.propertybilling.entity.TenantAssignment;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for tenant assignment records.
 */
public interface TenantAssignmentRepository extends JpaRepository<TenantAssignment, UUID> {

	/**
	 * Finds the active assignment for one unit with unit and tenant references loaded.
	 *
	 * @param unitId assigned unit identifier
	 * @return active assignment, or empty when the unit has no active tenant
	 */
	@Query("""
			SELECT tenantAssignment
			FROM TenantAssignment tenantAssignment
			JOIN FETCH tenantAssignment.unit unit
			JOIN FETCH tenantAssignment.tenant tenant
			WHERE unit.id = :unitId
			AND tenantAssignment.active = true
			AND tenantAssignment.endDate IS NULL
			""")
	Optional<TenantAssignment> findActiveByUnitId(@Param("unitId") UUID unitId);
}
