package com.helpdesk.service;

import com.helpdesk.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface CompanyService {
    Company createCompany(Company company);
    Company updateCompany(Long id, Company companyDetails);
    Company getCompanyById(Long id);
    List<Company> getAllCompanies();
    Page<Company> getCompanies(String search, Pageable pageable);
    void deleteCompany(Long id);
}
