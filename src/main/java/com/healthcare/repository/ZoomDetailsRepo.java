package com.healthcare.repository;

import com.healthcare.entity.ZoomDetails;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ZoomDetailsRepo extends JpaRepository<ZoomDetails, Long> {
    ZoomDetails findByAppointmentId(Long appointmentId);
}
