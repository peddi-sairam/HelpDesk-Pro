package com.helpdesk.service;

import com.helpdesk.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface DepartmentService {
    Department createDepartment(Long companyId, Department department);
    Department updateDepartment(Long id, Department departmentDetails);
    Department getDepartmentById(Long id);
    List<Department> getDepartmentsByCompany(Long companyId);
    Page<Department> getDepartmentsByCompany(Long companyId, String search, Pageable pageable);
    void deleteDepartment(Long id);
}
