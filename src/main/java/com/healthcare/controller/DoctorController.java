package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.DoctorRepo;
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
}
