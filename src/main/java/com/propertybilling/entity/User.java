package com.propertybilling.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "users")
public class User {

	@Id
	private UUID id;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(nullable = false)
	private String role;

	protected User() {
	}

	/**
	 * Creates a persisted user representation.
	 *
	 * @param id unique user identifier
	 * @param email login email address
	 * @param passwordHash hashed password
	 * @param role authorization role
	 */
	public User(
			UUID id,
			String email,
			String passwordHash,
			String role
	) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.role = role;
	}

	/**
	 * Returns the unique user identifier.
	 *
	 * @return user identifier
	 */
	public UUID getId() {
		return id;
	}

	/**
	 * Returns the email address used for login.
	 *
	 * @return login email address
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Returns the stored password hash.
	 *
	 * @return password hash
	 */
	public String getPasswordHash() {
		return passwordHash;
	}

	/**
	 * Returns the user's authorization role.
	 *
	 * @return authorization role
	 */
	public String getRole() {
		return role;
	}
}
