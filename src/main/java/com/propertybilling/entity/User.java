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
@Table(name = "users")
/*
 * Persisted admin or staff account used for authentication.
 */
public class User {

	@Id
	private UUID id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String role;

	@Column(name = "created_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
	private OffsetDateTime updatedAt;

	/**
	 * Creates a persisted user representation.
	 *
	 * @param id unique user identifier
	 * @param name display name
	 * @param email login email address
	 * @param passwordHash hashed password
	 * @param role authorization role
	 */
	public User(
			UUID id,
			String name,
			String email,
			String passwordHash,
			String role
	) {
		this(id, name, email, passwordHash, role, null, null);
	}

	/**
	 * Creates a persisted user representation with explicit timestamps.
	 *
	 * @param id unique user identifier
	 * @param name display name
	 * @param email login email address
	 * @param passwordHash hashed password
	 * @param role authorization role
	 * @param createdAt creation timestamp
	 * @param updatedAt latest update timestamp
	 */
	public User(
			UUID id,
			String name,
			String email,
			String passwordHash,
			String role,
			OffsetDateTime createdAt,
			OffsetDateTime updatedAt
	) {
		this.id = id;
		this.name = name;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}
}
