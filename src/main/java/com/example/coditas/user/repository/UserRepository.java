package com.example.coditas.user.repository;

import com.example.coditas.user.entity.User;
import com.example.coditas.common.enums.ActiveStatus;
import com.example.coditas.common.enums.UserRole;
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


    // AUTH & UNIQUES
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId);
    Optional<User> findActiveByUserId(String userId);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    Optional<User> findActiveById(Long id);


    Optional<User> findTopByOrderByCreatedAtDesc();

    Page<User> findByRoleNameAndIsActive(UserRole roleName, ActiveStatus isActive, Pageable pageable);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role.name IN :roles AND u.isActive = 'ACTIVE'")
    long countEmployeesByRolesAndActive(@Param("roles") List<UserRole> roles);

    // CUSTOMER: Find by email (auto-created)
    Optional<User> findByEmailAndRole_Name(String email, String roleName);

    // DASHBOARD COUNTS (fast)
    long countByIsActive(ActiveStatus status);
    long countByRole_NameAndIsActive(UserRole roleName, ActiveStatus status);


    // SOFT DELETE SUPPORT
    Page<User> findByIsActive(ActiveStatus status, Pageable pageable);
}
