package com.propertybilling.repository;

import com.propertybilling.entity.TenantUnitCredit;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/*
 * Data access boundary for tenant/unit credit balances.
 */
public interface TenantUnitCreditRepository extends JpaRepository<TenantUnitCredit, UUID> {

	/**
	 * Finds one credit balance with a write lock for settlement.
	 *
	 * @param tenantId tenant identifier
	 * @param unitId unit identifier
	 * @return matching credit when it exists
	 */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
			SELECT credit
			FROM TenantUnitCredit credit
			JOIN FETCH credit.tenant
			JOIN FETCH credit.unit
			WHERE credit.tenant.id = :tenantId
			AND credit.unit.id = :unitId
			""")
	Optional<TenantUnitCredit> findByTenantIdAndUnitIdForUpdate(
			@Param("tenantId") UUID tenantId,
			@Param("unitId") UUID unitId
	);
}
