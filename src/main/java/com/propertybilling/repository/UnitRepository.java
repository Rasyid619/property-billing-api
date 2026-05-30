package com.propertybilling.repository;

import com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult;
import com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult;
import com.propertybilling.entity.Unit;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for unit records.
 */
public interface UnitRepository extends JpaRepository<Unit, UUID> {

	/**
	 * Checks whether a unit number is already used inside one property.
	 *
	 * @param propertyId owning property identifier
	 * @param unitNumber submitted unit number
	 * @return true when the unit number already exists for the property
	 */
	@Query("""
			SELECT CASE WHEN COUNT(unit) > 0 THEN true ELSE false END
			FROM Unit unit
			WHERE unit.property.id = :propertyId
			AND unit.unitNumber = :unitNumber
			""")
	boolean existsByPropertyIdAndUnitNumber(
			@Param("propertyId") UUID propertyId,
			@Param("unitNumber") String unitNumber
	);

	/**
	 * Checks whether another unit already uses the submitted number inside one property.
	 *
	 * @param propertyId owning property identifier
	 * @param unitNumber submitted unit number
	 * @param unitId current unit identifier to exclude from the check
	 * @return true when another unit already uses the number for the property
	 */
	@Query("""
			SELECT CASE WHEN COUNT(unit) > 0 THEN true ELSE false END
			FROM Unit unit
			WHERE unit.property.id = :propertyId
			AND unit.unitNumber = :unitNumber
			AND unit.id <> :unitId
			""")
	boolean existsByPropertyIdAndUnitNumberAndIdNot(
			@Param("propertyId") UUID propertyId,
			@Param("unitNumber") String unitNumber,
			@Param("unitId") UUID unitId
	);

	/**
	 * Finds unit index rows for one property using an optional active-state filter.
	 *
	 * @param propertyId owning property identifier
	 * @param active optional active-state filter
	 * @param pageable pagination settings
	 * @return matching rows ordered by unit number and identifier
	 */
	@Query("""
			SELECT new com.propertybilling.dto.unit.queryresult.UnitIndexQueryResult(
			    unit.id,
			    property.id,
			    unit.unitNumber,
			    unit.monthlyFee,
			    unit.dueDay,
			    unit.active
			)
			FROM Unit unit
			JOIN unit.property property
			WHERE property.id = :propertyId
			AND (
			    :active IS NULL
			    OR unit.active = :active
			)
			ORDER BY unit.unitNumber ASC, unit.id ASC
			""")
	List<UnitIndexQueryResult> findIndexByPropertyId(
			@Param("propertyId") UUID propertyId,
			@Param("active") Boolean active,
			Pageable pageable
	);

	/**
	 * Finds one unit and its owning property for read-only detail workflows.
	 *
	 * @param unitId unit identifier
	 * @return matching unit, or empty when it does not exist
	 */
	@Query("""
			SELECT unit
			FROM Unit unit
			JOIN FETCH unit.property property
			WHERE unit.id = :unitId
			""")
	Optional<Unit> findByIdWithProperty(@Param("unitId") UUID unitId);

	/**
	 * Finds one unit using a write lock for mutation workflows.
	 *
	 * @param unitId unit identifier
	 * @return matching unit, or empty when it does not exist
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT unit
			FROM Unit unit
			WHERE unit.id = :unitId
			""")
	Optional<Unit> findByIdForUpdate(@Param("unitId") UUID unitId);

	/**
	 * Finds active units with active tenants for monthly invoice generation.
	 *
	 * @param propertyId owning property identifier
	 * @return active unit and tenant pairs ordered by unit number and identifier
	 */
	@Query("""
			SELECT new com.propertybilling.dto.invoice.queryresult.InvoiceGenerationTargetQueryResult(
			    unit.id,
			    tenant.id,
			    unit.unitNumber,
			    unit.monthlyFee,
			    unit.dueDay
			)
			FROM Unit unit
			JOIN unit.property property
			JOIN TenantAssignment tenantAssignment ON tenantAssignment.unit = unit
			JOIN tenantAssignment.tenant tenant
			WHERE property.id = :propertyId
			AND unit.active = true
			AND tenantAssignment.active = true
			AND tenantAssignment.endDate IS NULL
			ORDER BY unit.unitNumber ASC, unit.id ASC
			""")
	List<InvoiceGenerationTargetQueryResult> findMonthlyInvoiceGenerationTargets(
			@Param("propertyId") UUID propertyId
	);
}
