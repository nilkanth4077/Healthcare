package com.healthcare.repository;

import com.healthcare.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepo extends JpaRepository<Doctor, Long> {

    @Query("SELECT d FROM Doctor d WHERE LOWER(d.specialization) = LOWER(:speciality) AND LOWER(d.verificationStatus) = 'verified'")
    List<Doctor> findVerifiedDoctorsBySpecialization(@Param("speciality") String speciality);

    Optional<Doctor> findByUserId(Long userID);
}
