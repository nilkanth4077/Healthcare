package com.healthcare.service;

import com.healthcare.dto.StandardDTO;
import com.healthcare.dto.UpdateSlotResponse;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.DoctorSlot;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.DoctorRepo;
import com.healthcare.repository.DoctorSlotRepository;
import com.healthcare.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DoctorSlotService {

    private final DoctorRepo doctorRepository;
    private final DoctorSlotRepository slotRepository;
    private final HttpServletRequest request;
    private final AuditLogRepo auditLogRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public Map<String, Object> addSlots(
            Long doctorId,
            String slotType,
            LocalDate startDate,
            LocalDate endDate,
            String token,
            LocalTime... times) throws UserException {

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));
        List<DoctorSlot> slotsToAdd = new ArrayList<>();

        if (!sessionUser.getEmail().equals(doctor.getUser().getEmail())) {
            throw new RuntimeException("You are not allowed to add slots of other doctor");
        }

        if (!"VERIFIED".equalsIgnoreCase(doctor.getVerificationStatus())) {
            throw new IllegalStateException("Doctor is not verified yet");
        }

        List<DoctorSlot> slotsToSave = new ArrayList<>();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (LocalTime time : times) {
                LocalDateTime startDateTime = LocalDateTime.of(date, time);
                LocalDateTime endDateTime = startDateTime.plusMinutes(30);

                if (startDateTime.isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException(
                            "Can not add slots in past for date " + startDateTime.toString().substring(0, 10) + " from " + startDateTime.toString().substring(11, 16) + " to " + endDateTime.toString().substring(11, 16)
                    );
                }

                boolean overlaps = !slotRepository
                        .findOverlappingSlots(doctorId, slotType, startDateTime, endDateTime)
                        .isEmpty();

                boolean overlapsPending = slotsToSave.stream().anyMatch(s ->
                        s.getStartTime().isBefore(endDateTime) &&
                                s.getEndTime().isAfter(startDateTime)
                );

                if (overlaps || overlapsPending) {
                    throw new IllegalArgumentException(
                            "Slot overlap detected for " + startDateTime.toString().substring(0, 10) + " from " + startDateTime.toString().substring(11, 16) + " to " + endDateTime.toString().substring(11, 16)
                    );
                }

                slotsToSave.add(new DoctorSlot(null, doctor, slotType, startDateTime, endDateTime, true));
            }
        }

        slotRepository.saveAll(slotsToSave);

        return Map.of(
                "createdSlots", slotsToSave.size()
        );
    }

    public UpdateSlotResponse updateSlot(Long slotId, LocalDateTime newStartTime, String slotType, Boolean available, String token) {
        DoctorSlot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot not found"));
        String doctorEmail = jwtUtil.extractEmail(token);

        if (!slot.getDoctor().getUser().getEmail().equals(doctorEmail)) {
            throw new SecurityException("You are not allowed to update this slot");
        }

        if (newStartTime != null) {
            LocalDateTime newEndTime = newStartTime.plusMinutes(30);

            boolean overlaps = !slotRepository
                    .findOverlappingSlots(slot.getDoctor().getId(), slot.getSlotType(), newStartTime, newEndTime)
                    .isEmpty();

            if (overlaps) {
                throw new IllegalStateException("Slot overlaps with existing one");
            }

            slot.setStartTime(newStartTime);
            slot.setEndTime(newEndTime);
        }

        if (slotType != null && !slotType.isBlank()) {
            slot.setSlotType(slotType);
        }

        if (available != null) {
            slot.setAvailable(available);
        }

        slotRepository.save(slot);

        UpdateSlotResponse response = new UpdateSlotResponse();
        response.setDoctorId(slot.getDoctor().getId());
        response.setSlotId(slot.getId());
        response.setSlotType(slot.getSlotType());
        response.setAvailable(slot.isAvailable());
        response.setStartTime(slot.getStartTime());
        response.setEndTime(slot.getEndTime());

        return response;
    }

    public StandardDTO<Map<String, Object>> getSlotWithDoctor(Long slotId) {
        DoctorSlot slot = slotRepository.findSlotWithDoctor(slotId);

        Doctor doctor = slot.getDoctor();

        Map<String, Object> data = Map.of(
                "slotId", slot.getId(),
                "slotType", slot.getSlotType(),
                "startTime", slot.getStartTime(),
                "endTime", slot.getEndTime(),
                "available", slot.isAvailable(),
                "doctor", Map.of(
                        "doctorId", doctor.getId(),
                        "specialization", doctor.getSpecialization(),
                        "verificationStatus", doctor.getVerificationStatus(),
                        "user", doctor.getUser()
                )
        );

        return new StandardDTO<>(200, "Slot with doctor details", data, null);
    }

    public void deleteSlot(Long id, String token) throws UserException {

        DoctorSlot slot = slotRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Slot not found with id: " + id));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (!slotRepository.existsById(id)) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Deletion failed for slot: " + slot.getId());
            log.setAction("Slot Deletion Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("DOCTOR");
            log.setActorId(sessionUser.getId());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
            throw new RuntimeException("Slot not found with id " + id);
        } else if (!sessionUser.getEmail().equals(slot.getDoctor().getUser().getEmail())) {
            throw new RuntimeException("You are not allowed to delete this slot");
        }

        AuditLog log = new AuditLog();
        log.setActorId(null);
        log.setMessage("Deletion successful for slot: " + slot.getId());
        log.setAction("Slot Deletion Successful");
        log.setIpAddress(request.getRemoteAddr());
        log.setActorRole("DOCTOR");
        log.setActorId(sessionUser.getId());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        slotRepository.deleteById(id);

    }
}