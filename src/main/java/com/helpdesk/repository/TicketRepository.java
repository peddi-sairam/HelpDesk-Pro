package com.helpdesk.repository;

import com.helpdesk.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    Page<Ticket> findByCustomerId(Long customerId, Pageable pageable);
    Page<Ticket> findByCompanyId(Long companyId, Pageable pageable);
    Page<Ticket> findByAgentId(Long agentId, Pageable pageable);
    Page<Ticket> findByDepartmentId(Long departmentId, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.customer.id = :customerId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchByCustomer(@Param("customerId") Long customerId, @Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.company.id = :companyId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchByCompany(@Param("companyId") Long companyId, @Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.agent.id = :agentId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchByAgent(@Param("agentId") Long agentId, @Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE t.department.id = :deptId AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchByDepartment(@Param("deptId") Long deptId, @Param("search") String search, Pageable pageable);

    @Query("SELECT t FROM Ticket t WHERE (LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(t.category) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Ticket> searchAllTickets(@Param("search") String search, Pageable pageable);
}
