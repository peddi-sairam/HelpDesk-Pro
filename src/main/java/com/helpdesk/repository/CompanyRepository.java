package com.helpdesk.repository;

import com.helpdesk.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long>, JpaSpecificationExecutor<Company> {
    Optional<Company> findByName(String name);

    @Query("SELECT c FROM Company c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(c.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Company> searchCompanies(@Param("search") String search, Pageable pageable);
}
