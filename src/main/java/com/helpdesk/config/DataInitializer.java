package com.helpdesk.config;

import com.helpdesk.entity.*;
import com.helpdesk.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Initialize Default Roles
        initializeRoles();

        // Initialize Default Company
        Company techNova = initializeCompany();

        // Initialize Seed Users
        initializeUsers(techNova);
    }

    private void initializeRoles() {
        for (RoleName roleName : RoleName.values()) {
            Optional<Role> roleOpt = roleRepository.findByName(roleName);
            if (roleOpt.isEmpty()) {
                Role role = Role.builder().name(roleName).build();
                roleRepository.save(role);
            }
        }
    }

    private Company initializeCompany() {
        Optional<Company> companyOpt = companyRepository.findByName("TechNova Solutions");
        if (companyOpt.isEmpty()) {
            Company company = Company.builder()
                    .name("TechNova Solutions")
                    .email("info@technova.com")
                    .phone("123-456-7890")
                    .address("100 Innovation Way, Tech City")
                    .active(true)
                    .build();
            return companyRepository.save(company);
        }
        return companyOpt.get();
    }

    private void initializeUsers(Company company) {
        Role superAdminRole = roleRepository.findByName(RoleName.ROLE_SUPER_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role ROLE_SUPER_ADMIN not initialized"));
        Role companyAdminRole = roleRepository.findByName(RoleName.ROLE_COMPANY_ADMIN)
                .orElseThrow(() -> new RuntimeException("Role ROLE_COMPANY_ADMIN not initialized"));
        Role agentRole = roleRepository.findByName(RoleName.ROLE_SUPPORT_AGENT)
                .orElseThrow(() -> new RuntimeException("Role ROLE_SUPPORT_AGENT not initialized"));
        Role customerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER)
                .orElseThrow(() -> new RuntimeException("Role ROLE_CUSTOMER not initialized"));

        // 1. Super Admin
        if (!userRepository.existsByEmail("admin@helpdesk.com")) {
            User superAdmin = User.builder()
                    .fullName("System Administrator")
                    .email("admin@helpdesk.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("999-999-9999")
                    .role(superAdminRole)
                    .active(true)
                    .build();
            userRepository.save(superAdmin);
        }

        // 2. Company Admin
        if (!userRepository.existsByEmail("companyadmin@technova.com")) {
            User companyAdmin = User.builder()
                    .fullName("TechNova Administrator")
                    .email("companyadmin@technova.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .phone("888-888-8888")
                    .role(companyAdminRole)
                    .company(company)
                    .active(true)
                    .build();
            userRepository.save(companyAdmin);
        }

        // 3. Agent
        if (!userRepository.existsByEmail("agent1@technova.com")) {
            User agent = User.builder()
                    .fullName("Sarah Connor")
                    .email("agent1@technova.com")
                    .password(passwordEncoder.encode("Agent@123"))
                    .phone("777-777-7777")
                    .role(agentRole)
                    .company(company)
                    .active(true)
                    .build();
            userRepository.save(agent);
        }

        // 4. Customer
        if (!userRepository.existsByEmail("customer1@gmail.com")) {
            User customer = User.builder()
                    .fullName("John Doe")
                    .email("customer1@gmail.com")
                    .password(passwordEncoder.encode("Customer@123"))
                    .phone("666-666-6666")
                    .role(customerRole)
                    .company(company)
                    .active(true)
                    .build();
            userRepository.save(customer);
        }
    }
}
