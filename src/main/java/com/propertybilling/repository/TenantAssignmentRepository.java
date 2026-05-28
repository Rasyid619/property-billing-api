package com.propertybilling.repository;

import com.propertybilling.entity.TenantAssignment;
import java.util.List;
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
	 * Checks whether one unit already has an active tenant assignment.
	 *
	 * @param unitId assigned unit identifier
	 * @return true when an active assignment already exists
	 */
	@Query("""
			SELECT CASE WHEN COUNT(tenantAssignment) > 0 THEN true ELSE false END
			FROM TenantAssignment tenantAssignment
			WHERE tenantAssignment.unit.id = :unitId
			AND tenantAssignment.active = true
			AND tenantAssignment.endDate IS NULL
			""")
	boolean existsActiveByUnitId(@Param("unitId") UUID unitId);

	/**
	 * Finds tenant assignment history for one unit.
	 *
	 * @param unitId assigned unit identifier
	 * @return matching assignments ordered by newest start date first
	 */
	@Query("""
			SELECT tenantAssignment
			FROM TenantAssignment tenantAssignment
			JOIN FETCH tenantAssignment.unit unit
			JOIN FETCH tenantAssignment.tenant tenant
			WHERE unit.id = :unitId
			ORDER BY tenantAssignment.startDate DESC, tenantAssignment.id DESC
			""")
	List<TenantAssignment> findHistoryByUnitId(@Param("unitId") UUID unitId);

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
