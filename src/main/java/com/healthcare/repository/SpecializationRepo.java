package com.healthcare.repository;

import com.healthcare.entity.Specialization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpecializationRepo extends JpaRepository<Specialization, Long> {
}
