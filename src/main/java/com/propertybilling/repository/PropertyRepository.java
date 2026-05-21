package com.propertybilling.repository;

import com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult;
import com.propertybilling.entity.Property;
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
 * Data access boundary for property records.
 */
public interface PropertyRepository extends JpaRepository<Property, UUID> {

	/**
	 * Finds property index rows using optional search and active-state filters.
	 *
	 * @param searchPattern optional LIKE pattern matched against name and address
	 * @param active optional active-state filter
	 * @param pageable pagination settings
	 * @return matching rows ordered by name and identifier
	 */
	@Query("""
			SELECT new com.propertybilling.dto.property.queryresult.PropertyIndexQueryResult(
			    property.id,
			    property.name,
			    property.address,
			    property.active
			)
			FROM Property property
			WHERE (
			    :searchPattern IS NULL
			    OR property.name ILIKE :searchPattern
			    OR COALESCE(property.address, '') ILIKE :searchPattern
			)
			AND (
			    :active IS NULL
			    OR property.active = :active
			)
			ORDER BY property.name ASC, property.id ASC
			""")
	List<PropertyIndexQueryResult> findIndex(
			@Param("searchPattern") String searchPattern,
			@Param("active") Boolean active,
			Pageable pageable
	);

	/**
	 * Finds one property using a write lock for mutation workflows.
	 *
	 * @param propertyId property identifier
	 * @return matching property, or empty when it does not exist
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT property
			FROM Property property
			WHERE property.id = :propertyId
			""")
	Optional<Property> findByIdForUpdate(@Param("propertyId") UUID propertyId);
}
