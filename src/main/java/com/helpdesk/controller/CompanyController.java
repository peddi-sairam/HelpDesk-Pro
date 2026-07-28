package com.helpdesk.controller;

import com.helpdesk.dto.ApiResponse;
import com.helpdesk.entity.Company;
import com.helpdesk.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @PostMapping
    public ResponseEntity<ApiResponse<Company>> createCompany(@RequestBody Company company) {
        Company created = companyService.createCompany(company);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company created successfully", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> updateCompany(@PathVariable Long id, @RequestBody Company company) {
        Company updated = companyService.updateCompany(id, company);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company updated successfully", updated));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Company>> getCompanyById(@PathVariable Long id) {
        Company company = companyService.getCompanyById(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company fetched successfully", company));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Company>>> getCompanies(
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Company> companies = companyService.getCompanies(search, pageable);
        return ResponseEntity.ok(new ApiResponse<>(true, "Companies list fetched successfully", companies));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Company>>> getAllCompanies() {
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(new ApiResponse<>(true, "All companies list fetched successfully", companies));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCompany(@PathVariable Long id) {
        companyService.deleteCompany(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "Company deleted successfully"));
    }
}
