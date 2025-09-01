package com.healthcare.controller;

import com.healthcare.dto.*;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.UserRepo;
import com.healthcare.service.DoctorService;
import com.healthcare.service.UserService;
import com.healthcare.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepo userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogRepo auditLogRepo;
    private final DoctorService doctorService;
    private final HttpServletRequest request;

    public AuthController(AuthenticationManager authenticationManager, UserRepo userRepository, UserService userService,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuditLogRepo auditLogRepo, HttpServletRequest request, DoctorService doctorService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogRepo = auditLogRepo;
        this.request = request;
        this.doctorService = doctorService;
    }

    @PostMapping("/signup")
    public StandardDTO<User> signup(@RequestBody SignupDTO userRequest) throws UserException {

        if (!userRequest.getPassword().equals(userRequest.getConfirmPassword())) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Signup failed: Passwords do not match for " + userRequest.getEmail());
            log.setAction("User Registration Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("UNKNOWN");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepo.save(log);

            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Passwords do not match");
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Signup failed: Email already exists - " + userRequest.getEmail());
            log.setAction("User Registration Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("UNKNOWN");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepo.save(log);

            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.CONFLICT.value());
            response.setMessage("Email already exists: " + userRequest.getEmail());
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

        // Map UserRequest & User entity
        User user = new User();
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setEmail(userRequest.getEmail());
        user.setMobile(userRequest.getMobile());
        user.setRole(userRequest.getRole());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setActive(true);

        userRepository.save(user);

        AuditLog log = new AuditLog();
        log.setActorId(user.getId());
        log.setMessage("User registered successfully: " + user.getFirstName());
        log.setAction("User Registration");
        log.setIpAddress(request.getRemoteAddr());
        log.setActorRole(user.getRole());
        log.setTargetEntity("Doctor");
        log.setTargetId(user.getId());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepo.save(log);

        StandardDTO<User> response = new StandardDTO<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("User registered successfully");
        response.setData(user);
        response.setMetadata(null);
        return response;
    }


    @Operation(
            summary = "Register new doctor",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "multipart/form-data",
                            schema = @Schema(implementation = DoctorMultipartSchema.class)
                    )
            )
    )
    @PostMapping(value = "/register-doctor")
    public ResponseEntity<StandardDTO<?>> registerDoctor(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            @RequestParam("mobile") String mobile,
            @RequestParam("specialization") String specialization,
            @RequestParam("documentFile") MultipartFile documentFile
    ) throws Exception {
        StandardDTO<?> response = doctorService.registerDoctor(firstName, lastName, email, password, confirmPassword, mobile, specialization, documentFile);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<StandardDTO<Map<String, Object>>> login(@RequestBody LoginRequest loginRequest) {
        AuditLog log = new AuditLog();
        User userByEmail = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + loginRequest.getEmail()));

        String email = loginRequest.getEmail();

        try {
            if (userByEmail.getRole().equalsIgnoreCase("DOCTOR")) {
                Map<String, Object> doctor = doctorService.findDoctorByUserId(userByEmail.getId());
                if (!"Verified".equalsIgnoreCase(doctor.get("verificationStatus").toString())) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                            new StandardDTO<>(HttpStatus.UNAUTHORIZED.value(), "Doctor is not verified yet", null, null)
                    );
                }
            }

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword()));

            // Generate token and get role
            String token = jwtUtil.generateToken(
                    authentication.getName(),
                    authentication.getAuthorities().iterator().next().getAuthority()
            );
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            log.setAction("Login");
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(request.getRemoteAddr());
            log.setActorId(userByEmail.getId());
            log.setActorRole(role);
            log.setMessage("'" + email + "' logged in successfully");
            auditLogRepo.save(log);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user", userByEmail);

            return ResponseEntity.ok(
                    new StandardDTO<>(HttpStatus.OK.value(), "Slots added successfully", responseData, null)
            );

        } catch (AuthenticationException ex) {
            log.setAction("Failed Login");
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(request.getRemoteAddr());
            log.setActorId(userByEmail.getId());
            log.setActorRole(userByEmail.getEmail());
            log.setMessage("Failed login attempt with email '" + email + "'");
            auditLogRepo.save(log);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new StandardDTO<>(HttpStatus.UNAUTHORIZED.value(), ex.getMessage(), null, null)
            );
        }
    }

    @GetMapping("/profile")
    public Optional<User> getProfileByToken(@RequestHeader("Authorization") String authorizationHeader) throws UserException {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            return userService.getProfileByToken(token);
        } else {
            throw new UserException("Invalid Authorization header");
        }
    }
}
