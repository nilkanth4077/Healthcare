package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecializationIgnoreCase(String speciality);

    Optional<Doctor> findByUserId(Long userID);
}
