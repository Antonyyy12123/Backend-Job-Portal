package com.ey.repository;

import com.ey.entity.PasswordResetToken;
import com.ey.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
	Optional<PasswordResetToken> findByToken(String token);

	Optional<PasswordResetToken> findByUser(User user);

	void deleteByToken(String token);

	void deleteByUser(User user);
}
