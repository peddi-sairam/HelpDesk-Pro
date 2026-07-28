package com.helpdesk.repository;

import com.helpdesk.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long>, JpaSpecificationExecutor<Department> {
    List<Department> findByCompanyId(Long companyId);
    Page<Department> findByCompanyId(Long companyId, Pageable pageable);
    Optional<Department> findByNameAndCompanyId(String name, Long companyId);

    @Query("SELECT d FROM Department d WHERE d.company.id = :companyId AND " +
           "(LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(d.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Department> searchDepartments(
            @Param("companyId") Long companyId,
            @Param("search") String search,
            Pageable pageable);
}
