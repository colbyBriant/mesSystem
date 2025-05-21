package com.mesSystem.server.repository;

import com.mesSystem.server.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;

public interface UserRepository extends JpaRepository<User, String> {  // 修改泛型参数为String
    Optional<User> findByUsername(String username);
    List<User> findByUsernameContaining(String username);
    boolean existsByUsername(String username);
}