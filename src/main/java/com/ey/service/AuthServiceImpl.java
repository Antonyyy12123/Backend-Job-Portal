package com.ey.service;

import com.ey.dto.*;
import com.ey.entity.User;
import com.ey.entity.Role;
import com.ey.entity.HrStatus;
import com.ey.entity.PasswordResetToken;
import com.ey.exception.*;
import com.ey.repository.UserRepository;
import com.ey.repository.PasswordResetTokenRepository;
import com.ey.security.JwtService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

	private final UserRepository userRepo;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;
	private final JavaMailSender mailSender;
	private final PasswordResetTokenRepository tokenRepo;
	private final String resetUrlPrefix;

	private final long tokenExpiryMinutes;

	public AuthServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService,
			JavaMailSender mailSender, PasswordResetTokenRepository tokenRepo,
			@Value("${app.reset-url-prefix:https://example.com/reset-password?token=}") String resetUrlPrefix,
			@Value("${app.reset-token-expiry-minutes:60}") long tokenExpiryMinutes) {
		this.userRepo = userRepo;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
		this.mailSender = mailSender;
		this.tokenRepo = tokenRepo;
		this.resetUrlPrefix = resetUrlPrefix;
		this.tokenExpiryMinutes = tokenExpiryMinutes;
	}

	@Override
	@Transactional
	public RegisterResponse register(RegisterRequest request) {
		if (request == null)
			throw new BadRequestException("Request body is required");

		if (request.getRole() == null || request.getRole().isBlank()) {
			throw new BadRequestException("Role is required");
		}

		String roleStr = request.getRole().trim().toUpperCase();

		Role role;
		try {
			role = Role.valueOf(roleStr);
		} catch (IllegalArgumentException ex) {
			throw new BadRequestException("Invalid role. Allowed values: SEEKER, HR, ADMIN");
		}

		if (role == Role.ADMIN) {
			throw new ForbiddenException("Cannot create admin accounts");
		}

		if (userRepo.existsByEmail(request.getEmail())) {
			throw new ConflictException("Email already exists");
		}

		User user = new User();
		user.setName(request.getName());
		user.setEmail(request.getEmail());
		user.setPassword(passwordEncoder.encode(request.getPassword()));
		user.setRole(role);

		if (role == Role.HR) {
			user.setHrStatus(HrStatus.PENDING);
		}

		userRepo.save(user);

		RegisterResponse resp = new RegisterResponse();
		resp.setMessage("User registered successfully");
		resp.setUserId(user.getId());
		return resp;
	}

	@Override
	public LoginResponse login(LoginRequest request) {
		User user = userRepo.findByEmail(request.getEmail())
				.orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
		if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
			throw new UnauthorizedException("Invalid credentials");
		}
		String token = jwtService.generateToken(user.getEmail());
		LoginResponse out = new LoginResponse();
		out.setToken(token);
		out.setRole(user.getRole().name());
		out.setEmail(user.getEmail());
		out.setMessage("Login successful");
		return out;
	}

	@Override
	@Transactional
	public SimpleResponse forgotPassword(ForgotPasswordRequest request) {
		if (request == null || request.getEmail() == null || request.getEmail().isBlank()) {
			throw new BadRequestException("Email is required");
		}

		Optional<User> opt = userRepo.findByEmail(request.getEmail());
		if (opt.isEmpty()) {

			return simpleResponse("If an account with that email exists, a reset link has been sent.");
		}

		User user = opt.get();

		tokenRepo.findByUser(user).ifPresent(t -> tokenRepo.delete(t));

		String token = UUID.randomUUID().toString();
		PasswordResetToken prt = new PasswordResetToken();
		prt.setToken(token);
		prt.setUser(user);
		prt.setCreatedAt(Instant.now());
		prt.setExpiresAt(Instant.now().plus(tokenExpiryMinutes, ChronoUnit.MINUTES));
		tokenRepo.save(prt);

		String resetLink = resetUrlPrefix + token;
		SimpleMailMessage message = new SimpleMailMessage();
		message.setTo(user.getEmail());
		message.setSubject("Password reset request");
		message.setText(buildResetEmailText(user.getName(), resetLink));

		try {
			mailSender.send(message);
		} catch (Exception ex) {

			tokenRepo.delete(prt);
			throw new RuntimeException("Failed to send password reset email", ex);
		}

		return simpleResponse("If an account with that email exists, a reset link has been sent.");
	}

	@Override
	@Transactional
	public SimpleResponse resetPassword(ResetPasswordRequest request) {
		if (request == null || request.getToken() == null || request.getToken().isBlank()) {
			throw new BadRequestException("Invalid or expired token");
		}
		if (request.getNewPassword() == null || request.getNewPassword().isBlank()) {
			throw new BadRequestException("New password is required");
		}

		PasswordResetToken prt = tokenRepo.findByToken(request.getToken())
				.orElseThrow(() -> new BadRequestException("Invalid or expired token"));

		if (prt.getExpiresAt().isBefore(Instant.now())) {
			tokenRepo.delete(prt);
			throw new BadRequestException("Token has expired");
		}

		User user = prt.getUser();
		if (user == null) {
			tokenRepo.delete(prt);
			throw new NotFoundException("User not found for token");
		}

		user.setPassword(passwordEncoder.encode(request.getNewPassword()));
		userRepo.save(user);

		tokenRepo.delete(prt);

		return simpleResponse("Password reset successful");
	}

	@Override
	@Transactional
	public void changePassword(String username, String oldPassword, String newPassword) {
		User user = userRepo.findByEmail(username).orElseThrow(() -> new NotFoundException("User not found"));
		if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
			throw new ForbiddenException("Old password does not match");
		}
		user.setPassword(passwordEncoder.encode(newPassword));
		userRepo.save(user);
	}

	private SimpleResponse simpleResponse(String msg) {
		SimpleResponse r = new SimpleResponse();
		r.setMessage(msg);
		return r;
	}

	private String buildResetEmailText(String name, String link) {
		StringBuilder sb = new StringBuilder();
		sb.append("Hello ");
		sb.append(name != null ? name : "");
		sb.append(",\n\n");
		sb.append("We received a request to reset your password. Click the link below to set a new password:\n\n");
		sb.append(link).append("\n\n");
		sb.append("If you did not request this, you can safely ignore this email.\n\n");
		sb.append("This link will expire in ").append(tokenExpiryMinutes).append(" minutes.\n\n");
		sb.append("Regards,\nJob Portal Team");
		return sb.toString();
	}
}
