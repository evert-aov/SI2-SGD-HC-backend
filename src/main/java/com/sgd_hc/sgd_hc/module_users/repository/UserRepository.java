package com.sgd_hc.sgd_hc.module_users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sgd_hc.sgd_hc.module_users.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByCi(String ci);
    Optional<User> findByEmail(String email);
    boolean existsByCi(String ci);
    boolean existsByEmail(String email);
    List<User> findAllByCi(String ci);
    List<User> findAllByEmail(String email);
}
