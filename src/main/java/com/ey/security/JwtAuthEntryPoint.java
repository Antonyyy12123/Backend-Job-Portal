package com.ey.security;

import com.ey.exception.ApiError;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper mapper = new ObjectMapper();

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException, ServletException {

		ApiError err = new ApiError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", authException.getMessage(),
				request.getRequestURI());
		err.setTimestamp(Instant.now());
		response.setStatus(HttpStatus.UNAUTHORIZED.value());
		response.setContentType("application/json");
		mapper.writeValue(response.getOutputStream(), err);
	}
}