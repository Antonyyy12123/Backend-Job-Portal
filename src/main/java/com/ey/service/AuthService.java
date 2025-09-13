package com.ey.service;
 
import com.ey.dto.*;
 
public interface AuthService {
    RegisterResponse register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    SimpleResponse forgotPassword(ForgotPasswordRequest request);
    SimpleResponse resetPassword(ResetPasswordRequest request);
    void changePassword(String username, String oldPassword, String newPassword);
}