package com.healthcare.controller;

import com.healthcare.dto.AppointmentResponse;
import com.healthcare.dto.BookedAppointment;
import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.*;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.service.AppointmentService;
import com.healthcare.service.DoctorService;
import com.healthcare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AuditLogRepo auditLogRepo;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private UserService userService;

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

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
        rs.setData("Hello Admin: " + user.getFirstName());
        rs.setMetadata(null);
        rs.setStatusCode(HttpStatus.OK.value());
        rs.setMessage("Hello response successful");

        return rs;
    }

    @PostMapping("/addSpeciality")
    public StandardDTO<?> add(@RequestBody Specialization specialization) {

        Specialization response = doctorService.addSpecialization(specialization);
        return new StandardDTO<>(HttpStatus.OK.value(), "Speciality added successfully", response, null);
    }

    @GetMapping("/all-doctors")
    public StandardDTO<List<Map<String, Object>>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Doctor doc : doctors) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("specialization", doc.getSpecialization());
            map.put("verificationStatus", doc.getVerificationStatus());
            map.put("doctor", doc.getUser());
            responseList.add(map);
        }
        return new StandardDTO<>(HttpStatus.OK.value(), "List of doctors fetched successfully", responseList, null);
    }

    @DeleteMapping("/delete/doctor")
    public StandardDTO<?> deleteDoctor(@RequestParam Long id, @RequestHeader("Authorization") String token) throws UserException {

        String actualToken = token.replace("Bearer ", "");

        doctorService.deleteDoctor(id, actualToken);

        Map<String, Object> res = new HashMap<>();
        res.put("id", id);

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctor deleted successfully", res, null);
    }

    @GetMapping("/get/all-users")
    public ResponseEntity<StandardDTO<List<User>>> getAllUsers(
            @RequestHeader("Authorization") String token
    ) {
        try {
            String actualToken = token.replace("Bearer ", "");
            List<User> users = userService.getAllUsers(actualToken);

            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Users fetched successfully", users, null)
            );
        } catch (UserException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), "Error fetching users", null, null)
            );
        }
    }

    @GetMapping("/get/all-appointments")
    public ResponseEntity<StandardDTO<List<BookedAppointment>>> getAllAppointments(
            @RequestHeader("Authorization") String token
    ) {
        try {
            String actualToken = token.replace("Bearer ", "");
            List<Appointment> appointments = appointmentService.getAllAppointments(actualToken);

            List<BookedAppointment> res = new ArrayList<>();

            for (Appointment a : appointments) {
                BookedAppointment ba = new BookedAppointment();
                ba.setAppointmentId(a.getId());
                ba.setUser(a.getUser());
                ba.setAppointmentStatus(a.getStatus());
                ba.setDoctorFirstName(a.getSlot().getDoctor().getUser().getFirstName());
                ba.setDoctorLastName(a.getSlot().getDoctor().getUser().getLastName());
                ba.setDoctorMobile(a.getSlot().getDoctor().getUser().getMobile());
                ba.setDoctorId(a.getSlot().getDoctor().getUser().getId());
                ba.setEndTime(a.getSlot().getEndTime());
                ba.setSpecialization(a.getSlot().getDoctor().getSpecialization());
                ba.setSlotId(a.getSlot().getId());
                ba.setSlotType(a.getSlot().getSlotType());
                ba.setStartTime(a.getSlot().getStartTime());
                ba.setDoctorEmail(a.getSlot().getDoctor().getUser().getEmail());

                res.add(ba);
            }

            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Appointments fetched successfully", res, null)
            );
        } catch (NoSuchFieldException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new StandardDTO<>(HttpStatus.BAD_REQUEST.value(), "Error fetching appointments", null, null)
            );
        }
    }
}
