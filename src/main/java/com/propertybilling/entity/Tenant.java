package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tenants")
/*
 * Persisted tenant or resident data record managed by admins and staff.
 */
public class Tenant {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	private String phone;

	private String email;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a new tenant that relies on database-managed timestamps.
	 *
	 * @param id unique tenant identifier
	 * @param name tenant display name
	 * @param phone optional phone number
	 * @param email optional email address
	 */
	public Tenant(
			UUID id,
			String name,
			String phone,
			String email
	) {
		this(id, name, phone, email, null, null);
	}

	/**
	 * Creates a persisted tenant representation.
	 *
	 * @param id unique tenant identifier
	 * @param name tenant display name
	 * @param phone optional phone number
	 * @param email optional email address
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public Tenant(
			UUID id,
			String name,
			String phone,
			String email,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.name = name;
		this.phone = phone;
		this.email = email;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

	/**
	 * Replaces tenant data fields.
	 *
	 * @param name new tenant display name
	 * @param phone new optional phone number
	 * @param email new optional email address
	 */
	public void update(String name, String phone, String email) {
		this.name = name;
		this.phone = phone;
		this.email = email;
	}
}
