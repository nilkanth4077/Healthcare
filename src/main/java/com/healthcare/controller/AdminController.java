package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Specialization;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.service.DoctorService;
import com.healthcare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

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
}
