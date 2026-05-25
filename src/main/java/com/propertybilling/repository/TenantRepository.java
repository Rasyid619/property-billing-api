package com.propertybilling.repository;

import com.propertybilling.dto.tenant.queryresult.TenantIndexQueryResult;
import com.propertybilling.entity.Tenant;
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
 * Data access boundary for tenant records.
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

	/**
	 * Checks whether a tenant already uses the phone number.
	 *
	 * @param phone phone number to check
	 * @return true when the phone number is already used
	 */
	boolean existsByPhone(String phone);

	/**
	 * Checks whether a tenant already uses the email address.
	 *
	 * @param email email address to check
	 * @return true when the email address is already used
	 */
	boolean existsByEmail(String email);

	/**
	 * Checks whether another tenant already uses the phone number.
	 *
	 * @param phone phone number to check
	 * @param tenantId tenant identifier to exclude
	 * @return true when another tenant already uses the phone number
	 */
	boolean existsByPhoneAndIdNot(String phone, UUID tenantId);

	/**
	 * Checks whether another tenant already uses the email address.
	 *
	 * @param email email address to check
	 * @param tenantId tenant identifier to exclude
	 * @return true when another tenant already uses the email address
	 */
	boolean existsByEmailAndIdNot(String email, UUID tenantId);

	/**
	 * Finds one tenant using a write lock for mutation workflows.
	 *
	 * @param tenantId tenant identifier
	 * @return matching tenant, or empty when it does not exist
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT tenant
			FROM Tenant tenant
			WHERE tenant.id = :tenantId
			""")
	Optional<Tenant> findByIdForUpdate(@Param("tenantId") UUID tenantId);

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
