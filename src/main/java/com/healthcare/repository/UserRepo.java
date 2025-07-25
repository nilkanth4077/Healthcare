package com.healthcare.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.healthcare.entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);

	boolean existsByRole(String role);

}
