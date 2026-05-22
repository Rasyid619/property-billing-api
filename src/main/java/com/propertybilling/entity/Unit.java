package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "units")
/*
 * Persisted rentable unit inside one property.
 */
public class Unit {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "property_id", nullable = false)
	private Property property;

	@Column(name = "unit_number", nullable = false)
	private String unitNumber;

	@Column(name = "monthly_fee", nullable = false)
	private String monthlyFee;

	@Column(name = "due_day", nullable = false)
	private int dueDay;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new unit that relies on database-managed timestamps.
	 *
	 * @param id unique unit identifier
	 * @param property owning property
	 * @param unitNumber display number unique inside one property
	 * @param monthlyFee decimal string monthly fee
	 * @param dueDay day of month when payment is due
	 * @param active whether the unit can be used for new workflows
	 */
	public Unit(
			UUID id,
			Property property,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active
	) {
		this(id, property, unitNumber, monthlyFee, dueDay, active, null, null);
	}

	/**
	 * Creates a persisted unit representation.
	 *
	 * @param id unique unit identifier
	 * @param property owning property
	 * @param unitNumber display number unique inside one property
	 * @param monthlyFee decimal string monthly fee
	 * @param dueDay day of month when payment is due
	 * @param active whether the unit can be used for new workflows
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public Unit(
			UUID id,
			Property property,
			String unitNumber,
			String monthlyFee,
			int dueDay,
			boolean active,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.property = property;
		this.unitNumber = unitNumber;
		this.monthlyFee = monthlyFee;
		this.dueDay = dueDay;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Replaces the editable fields of the unit.
	 *
	 * @param unitNumber new display number unique inside one property
	 * @param monthlyFee new decimal string monthly fee
	 * @param dueDay new day of month when payment is due
	 */
	public void update(String unitNumber, String monthlyFee, int dueDay) {
		this.unitNumber = unitNumber;
		this.monthlyFee = monthlyFee;
		this.dueDay = dueDay;
	}

	/**
	 * Marks the unit inactive.
	 */
	public void deactivate() {
		this.active = false;
	}

	/**
	 * Marks the unit active.
	 */
	public void activate() {
		this.active = true;
	}
}
