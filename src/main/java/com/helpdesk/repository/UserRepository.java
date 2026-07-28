package com.helpdesk.repository;

import com.helpdesk.entity.User;
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
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByCompanyId(Long companyId);
    Page<User> findByCompanyId(Long companyId, Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.company.id = :companyId AND " +
           "(LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchUsersByCompany(
            @Param("companyId") Long companyId,
            @Param("search") String search,
            Pageable pageable);

    @Query("SELECT u FROM User u WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<User> searchAllUsers(@Param("search") String search, Pageable pageable);
}
