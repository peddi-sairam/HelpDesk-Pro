package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.User;
import com.helpdesk.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<User>> createUser(@Valid @RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.ok(new ApiResponse<>(true, "User created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        User updated = userService.updateUser(id, userDetails);
        return ResponseEntity.ok(new ApiResponse<>(true, "User updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User fetched successfully", user));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<User>>> getUsersByCompany(@PathVariable Long companyId) {
        List<User> users = userService.getUsersByCompany(companyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users list fetched successfully", users));
    }

    @GetMapping("/company/{companyId}/page")
    public ResponseEntity<ApiResponse<Page<User>>> getUsersByCompanyPage(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getUsersByCompany(companyId, search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company users list fetched successfully", users));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<User>>> getAllUsers(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users = userService.getAllUsers(search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Users list fetched successfully", users));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Void>> changeUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        userService.changeUserStatus(id, active);
        return ResponseEntity.ok(new ApiResponse<>(true, "User status updated successfully"));
    }

    @PostMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "User deleted successfully"));
    }
}
