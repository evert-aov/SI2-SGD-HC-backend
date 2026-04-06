package com.sgd_hc.sgd_hc.module_users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.sgd_hc.sgd_hc.module_users.entity.Permission;

import java.util.Optional;
import java.util.Set;
import java.util.List;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);

    boolean existsByName(String name);

    Set<Long> findByIdIn(Set<Long> ids);

    Optional<Permission> findByModuleAndAction(String module, String action);

    List<Permission> findByActiveTrue();  
}
