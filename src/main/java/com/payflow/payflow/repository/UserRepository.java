package com.payflow.payflow.repository;

import com.payflow.payflow.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for the User entity.
 * Provides methods for performing CRUD operations and custom queries
 * on user data, such as finding users by username or role.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     * @param username The username to search for.
     * @return An Optional containing the User, or empty if no user is found.
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds all users with a specific role.
     * @param role The role to search for (e.g., "ADMIN", "HR", "MANAGER").
     * @return A List of User objects matching the specified role.
     */
    List<User> findByRole(String role);
}
