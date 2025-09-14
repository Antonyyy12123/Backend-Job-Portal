package com.ey.controller;
 
import com.ey.dto.*;
import com.ey.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
 
    private final AdminService adminService;
    public AdminController(AdminService adminService) { this.adminService = adminService; }
 
    @GetMapping("/hr/pending")
    public ResponseEntity<?> listPendingHrs() {
        return ResponseEntity.ok(adminService.getPendingHrs());
    }
 
    @PutMapping("/hr/{id}/approve")
    public ResponseEntity<?> approveHr(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.approveHr(id));
    }
 
    @PutMapping("/hr/{id}/reject")
    public ResponseEntity<?> rejectHr(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.rejectHr(id));
    }
 
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}