package com.helpdesk.service.impl;

import com.helpdesk.entity.Company;
import com.helpdesk.entity.Department;
import com.helpdesk.exception.BadRequestException;
import com.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.repository.CompanyRepository;
import com.helpdesk.repository.DepartmentRepository;
import com.helpdesk.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public Department createDepartment(Long companyId, Department department) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id " + companyId));

        Optional<Department> existing = departmentRepository.findByNameAndCompanyId(department.getName(), companyId);
        if (existing.isPresent()) {
            throw new BadRequestException("Department with name '" + department.getName() + "' already exists in this company");
        }

        department.setCompany(company);
        return departmentRepository.save(department);
    }

    @Override
    public Department updateDepartment(Long id, Department departmentDetails) {
        Department department = getDepartmentById(id);
        Long companyId = department.getCompany().getId();

        Optional<Department> existing = departmentRepository.findByNameAndCompanyId(departmentDetails.getName(), companyId);
        if (existing.isPresent() && !existing.get().getId().equals(id)) {
            throw new BadRequestException("Department with name '" + departmentDetails.getName() + "' already exists in this company");
        }

        department.setName(departmentDetails.getName());
        department.setDescription(departmentDetails.getDescription());
        return departmentRepository.save(department);
    }

    @Override
    public Department getDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id " + id));
    }

    @Override
    public List<Department> getDepartmentsByCompany(Long companyId) {
        return departmentRepository.findByCompanyId(companyId);
    }

    @Override
    public Page<Department> getDepartmentsByCompany(Long companyId, String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return departmentRepository.findByCompanyId(companyId, pageable);
        }
        return departmentRepository.searchDepartments(companyId, search, pageable);
    }

    @Override
    public void deleteDepartment(Long id) {
        departmentRepository.deleteById(id);
    }
}
