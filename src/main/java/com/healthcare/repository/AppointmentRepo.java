package com.healthcare.repository;

import com.healthcare.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepo extends JpaRepository<Appointment, Long> {
    List<Appointment> findBySlotDoctorId(Long doctorId);
}
