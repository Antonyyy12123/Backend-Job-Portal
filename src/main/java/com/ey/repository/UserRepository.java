package com.ey.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ey.entity.HrStatus;
import com.ey.entity.Role;
import com.ey.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	List<User> findByRole(Role role);

	List<User> findByRoleAndHrStatus(Role role, HrStatus hrStatus);
}