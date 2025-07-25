package com.e_commerce.controller;

import com.e_commerce.entity.AuditLog;
import com.e_commerce.entity.User;
import com.e_commerce.exception.UserException;
import com.e_commerce.repository.AuditLogRepo;
import com.e_commerce.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    @GetMapping("/hello")
    public String hello(@RequestHeader String token) throws UserException {

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

        return "Hello, Admin!";
    }
}
