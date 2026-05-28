package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "unit_tenants")
/*
 * Persisted assignment history between one unit and one tenant.
 */
public class TenantAssignment {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "unit_id", nullable = false)
	private Unit unit;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "tenant_id", nullable = false)
	private Tenant tenant;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a tenant assignment that relies on database-managed timestamps.
	 *
	 * @param id unique assignment identifier
	 * @param unit assigned unit
	 * @param tenant assigned tenant
	 * @param startDate first date of the assignment
	 * @param endDate optional final date of the assignment
	 * @param active whether the assignment is currently active
	 */
	public TenantAssignment(
			UUID id,
			Unit unit,
			Tenant tenant,
			LocalDate startDate,
			LocalDate endDate,
			boolean active
	) {
		this(id, unit, tenant, startDate, endDate, active, null, null);
	}

	/**
	 * Creates a persisted tenant assignment representation.
	 *
	 * @param id unique assignment identifier
	 * @param unit assigned unit
	 * @param tenant assigned tenant
	 * @param startDate first date of the assignment
	 * @param endDate optional final date of the assignment
	 * @param active whether the assignment is currently active
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public TenantAssignment(
			UUID id,
			Unit unit,
			Tenant tenant,
			LocalDate startDate,
			LocalDate endDate,
			boolean active,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.unit = unit;
		this.tenant = tenant;
		this.startDate = startDate;
		this.endDate = endDate;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Closes the assignment while preserving its history row.
	 *
	 * @param endDate final date of the assignment
	 */
	public void moveOut(LocalDate endDate) {
		this.endDate = endDate;
		this.active = false;
	}
}
