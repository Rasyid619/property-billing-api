package com.propertybilling.repository;

import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.entity.Tenant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for tenant records.
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

	/**
	 * Finds tenant index rows using an optional search filter.
	 *
	 * @param searchPattern optional LIKE pattern matched against name, phone, and email
	 * @param pageable pagination settings
	 * @return matching rows ordered by name and identifier
	 */
	@Query("""
			SELECT new com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult(
			    tenant.id,
			    tenant.name,
			    tenant.phone,
			    tenant.email
			)
			FROM Tenant tenant
			WHERE (
			    :searchPattern IS NULL
			    OR tenant.name ILIKE :searchPattern
			    OR COALESCE(tenant.phone, '') ILIKE :searchPattern
			    OR COALESCE(tenant.email, '') ILIKE :searchPattern
			)
			ORDER BY tenant.name ASC, tenant.id ASC
			""")
	List<TenantIndexQueryResult> findIndex(
			@Param("searchPattern") String searchPattern,
			Pageable pageable
	);
}
