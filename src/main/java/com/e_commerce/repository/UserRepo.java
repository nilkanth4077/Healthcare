package com.e_commerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.e_commerce.entity.User;

import java.util.Optional;

public interface UserRepo extends JpaRepository<User, Long>{

	Optional<User> findByEmail(String email);

	boolean existsByRole(String role);

}
