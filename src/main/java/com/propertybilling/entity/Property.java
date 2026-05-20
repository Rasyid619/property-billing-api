package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "properties")
/*
 * Persisted property or building managed by admins and staff.
 */
public class Property {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	private String address;

	@Column(name = "is_active", nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Property() {
	}

	/**
	 * Creates a persisted property representation.
	 *
	 * @param id unique property identifier
	 * @param name display name
	 * @param address optional property address
	 * @param active whether the property can be used for new workflows
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public Property(
			UUID id,
			String name,
			String address,
			boolean active,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.active = active;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Returns the unique property identifier.
	 *
	 * @return property identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns the property display name.
	 *
	 * @return display name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the optional property address.
	 *
	 * @return address or null
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Returns whether the property is active.
	 *
	 * @return active state
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Returns when the property was created.
	 *
	 * @return creation timestamp
	 */
	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	/**
	 * Returns when the property was last updated.
	 *
	 * @return update timestamp
	 */
	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
