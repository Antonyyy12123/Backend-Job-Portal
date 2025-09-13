package com.ey.dto;
 
public class LoginResponse {
    private String token;
    private String role;
    private String email;
    private String message;
 
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
 
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
 
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
 
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}