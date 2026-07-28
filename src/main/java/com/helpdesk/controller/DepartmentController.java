package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.Department;
import com.helpdesk.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Department>> createDepartment(
            @RequestParam Long companyId,
            @Valid @RequestBody Department department) {
        Department created = departmentService.createDepartment(companyId, department);
        return ResponseEntity.ok(new ApiResponse<>(true, "Department created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody Department departmentDetails) {
        Department updated = departmentService.updateDepartment(id, departmentDetails);
        return ResponseEntity.ok(new ApiResponse<>(true, "Department updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Department>> getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Department fetched successfully", department));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartmentsByCompany(@PathVariable Long companyId) {
        List<Department> departments = departmentService.getDepartmentsByCompany(companyId);
        return ResponseEntity.ok(new ApiResponse<>(true, "Departments fetched successfully", departments));
    }

    @GetMapping("/company/{companyId}/page")
    public ResponseEntity<ApiResponse<Page<Department>>> getDepartmentsByCompanyPage(
            @PathVariable Long companyId,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Department> departments = departmentService.getDepartmentsByCompany(companyId, search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Departments page fetched successfully", departments));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Department deleted successfully"));
    }
}
