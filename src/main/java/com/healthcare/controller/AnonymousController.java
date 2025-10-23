package com.healthcare.controller;

import com.healthcare.dto.*;
import com.healthcare.entity.Appointment;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Specialization;
import com.healthcare.exception.UserException;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.DoctorSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AnonymousController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private DoctorSlotService slotService;

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping("/speciality/all")
    public StandardDTO<List<Specialization>> getAll() {
        List<Specialization> res = doctorService.getAllSpecializations();
        return new StandardDTO<>(HttpStatus.OK.value(), "Specialities fetched successfully", res, null);
    }

    @GetMapping("/get/doctor")
    public StandardDTO<Map<String, Object>> getDoctorById(@RequestParam("docId") Long doctorId) {
        Doctor doc = doctorService.getDoctorById(doctorId)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with provided id"));

        Map<String, Object> res = new HashMap<>();
        res.put("doctorId", doc.getId());
        res.put("firstName", doc.getUser().getFirstName());
        res.put("lastName", doc.getUser().getLastName());
        res.put("email", doc.getUser().getEmail());
        res.put("mobile", doc.getUser().getMobile());
        res.put("specialization", doc.getSpecialization());
        res.put("verificationStatus", doc.getVerificationStatus());
        res.put("documentSize", doc.getDocument().length);

        if (doc.getDocument() != null) {
            String base64Doc = Base64.getEncoder().encodeToString(doc.getDocument());
            res.put("document", doc.getDocument());
        }

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctor fetched successfully with id: " + doctorId, res, null);
    }

    @PutMapping(value = "/update/doctor")
    public ResponseEntity<StandardDTO<UpdateDoctorResponse>> updateSlot(@RequestParam("doctorId") Long doctorId,
                                                                        @RequestParam(name = "firstName", required = false) String firstName,
                                                                        @RequestParam(name = "lastName", required = false) String lastName,
                                                                        @RequestParam(name = "email", required = false) String email,
                                                                        @RequestParam(name = "verificationStatus", required = false) String verificationStatus,
                                                                        @RequestParam(name = "specialization", required = false) String specialization,
                                                                        @RequestParam(name = "mobile", required = false) String mobile,
                                                                        @RequestParam(name = "active", required = false) Boolean active,
                                                                        @RequestParam(name = "documentFile", required = false) MultipartFile documentFile,
                                                                        @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            UpdateDoctorResponse response = doctorService.updateDoctorDetails(doctorId, firstName, lastName, email, verificationStatus, specialization, mobile, active, documentFile, actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Doctor details updated successfully", response, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @PutMapping("/slot/delete/expiredSlot")
    public ResponseEntity<StandardDTO<String>> deleteExpiredSlots(@RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            String res = slotService.deleteExpiredSlots(actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), res, null, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @PostMapping("/send/email/roomDetails")
    public ResponseEntity<StandardDTO<String>> sendRoomDetails(
            @RequestParam("appointmentId") Long appointmentId,
            @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            appointmentService.sendEmailWithRoomId(appointmentId, actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Check your mail box for room details", null, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/get/appointment")
    public ResponseEntity<StandardDTO<Map<String, Object>>> getAppointmentBySlotId(
            @RequestParam("slotId") Long slotId,
            @RequestHeader("Authorization") String token
    ) {
        try {
            String actualToken = token.replace("Bearer ", "");
            Appointment appointment = appointmentService.findAppointmentBySlotId(slotId, actualToken);

            Map<String, Object> res = new HashMap<>();
            res.put("appointmentId", appointment.getId());
            res.put("appointmentStatus", appointment.getStatus());

            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Check your mail box for room details", res, null)
            );
        } catch (UserException e) {
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Check your mail box for room details", null, null)
            );
        }
    }
}
