package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.DoctorRepo;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.EmailService;
import com.healthcare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
@PreAuthorize("hasRole('DOCTOR')")
public class DoctorController {

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/hello")
    public StandardDTO<String> hello(@RequestHeader String token) throws UserException {

        User user = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        AuditLog log = new AuditLog();

        log.setActorId(user.getId());
        log.setMessage(user.getFirstName() + " (" + user.getRole() + ") " + "said hello");
        log.setAction("Accessed hello URL");
        log.setActorRole(user.getRole());
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(request.getRemoteAddr());
        auditLogRepo.save(log);

        StandardDTO<String> rs = new StandardDTO<>();
        rs.setData("Hello Doctor: " + user.getFirstName());
        rs.setMetadata(null);
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Hello response successful");

        return rs;
    }

    @GetMapping("/by-user-id")
    public ResponseEntity<StandardDTO<Map<String, Object>>> getDoctorByUserId(@RequestParam Long userId) {
        try {
            Map<String, Object> res = doctorService.findDoctorByUserId(userId);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Doctor fetched successfully", res, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/get/booked-appointments")
    public ResponseEntity<StandardDTO<List<Map<String, Object>>>> getBookedAppointments(@RequestParam Long doctorId, @RequestHeader("Authorization") String token) {
        try {
            String actualToken = token.replace("Bearer ", "");
            List<Map<String, Object>> res = appointmentService.getAppointmentsForDoctor(doctorId, actualToken);
            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Booked slots fetched successfully", res, null)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null, null)
            );
        }
    }

    @PostMapping("/call/send-invite")
    public ResponseEntity<Map<String, Object>> sendInvite(
            @RequestParam String patientEmail,
            @RequestParam String roomName) {

        String meetingLink = "https://meet.jit.si/" + roomName;

        String subject = "Video Call Appointment Invitation";
        String body = "Dear Patient,\n\n" +
                "Your doctor has started a video call. Please join using the link below:\n\n" +
                meetingLink + "\n\nBest Regards,\nHealthcare System";

        emailService.sendSimpleMessage(patientEmail, subject, body);

        Map<String, Object> response = new HashMap<>();
        response.put("statusCode", 200);
        response.put("message", "Invite sent successfully");
        response.put("meetingLink", meetingLink);

        return ResponseEntity.ok(response);
    }
}
