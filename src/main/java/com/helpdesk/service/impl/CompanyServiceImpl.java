package com.helpdesk.service.impl;

import com.helpdesk.entity.Company;
import com.helpdesk.repository.CompanyRepository;
import com.helpdesk.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    @Override
    public Company createCompany(Company company) {
        return companyRepository.save(company);
    }

    @Override
    public Company updateCompany(Long id, Company companyDetails) {
        Company company = getCompanyById(id);
        company.setName(companyDetails.getName());
        company.setEmail(companyDetails.getEmail());
        company.setPhone(companyDetails.getPhone());
        company.setAddress(companyDetails.getAddress());
        company.setActive(companyDetails.isActive());
        return companyRepository.save(company);
    }

    @Override
    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id " + id));
    }

    @Override
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Override
    public Page<Company> getCompanies(String search, Pageable pageable) {
        if (search == null || search.trim().isEmpty()) {
            return companyRepository.findAll(pageable);
        }
        return companyRepository.searchCompanies(search, pageable);
    }

    @Override
    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
