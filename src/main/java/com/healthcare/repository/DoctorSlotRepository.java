package com.healthcare.repository;

import com.healthcare.entity.DoctorSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DoctorSlotRepository extends JpaRepository<DoctorSlot, Long> {

    @Query("SELECT s FROM DoctorSlot s WHERE s.doctor.id = :doctorId AND s.slotType = :slotType " +
            "AND ((s.startTime < :endTime AND s.endTime > :startTime))")
    List<DoctorSlot> findOverlappingSlots(Long doctorId, String slotType,
                                          LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM DoctorSlot s JOIN FETCH s.doctor d WHERE s.id = :slotId")
    DoctorSlot findSlotWithDoctor(Long slotId);

}