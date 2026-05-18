package com.propertybilling.repository;

import com.propertybilling.entity.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/*
 * Data access boundary for persisted admin and staff users.
 */
public interface UserRepository extends JpaRepository<User, UUID> {

	/**
	 * Finds a user account by its unique email address.
	 *
	 * @param email email address used for login
	 * @return matching user when one exists
	 */
	Optional<User> findByEmail(String email);
}
