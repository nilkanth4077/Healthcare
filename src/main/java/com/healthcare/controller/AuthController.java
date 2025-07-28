package com.healthcare.controller;

import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.SignupDTO;
import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.UserRepo;
import com.healthcare.service.UserService;
import com.healthcare.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

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
    private final HttpServletRequest request;

    public AuthController(AuthenticationManager authenticationManager, UserRepo userRepository, UserService userService,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AuditLogRepo auditLogRepo, HttpServletRequest request) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.auditLogRepo = auditLogRepo;
        this.request = request;
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

        userRepository.save(user);

        AuditLog log = new AuditLog();
        log.setActorId(user.getId());
        log.setMessage("User registered successfully: " + user.getFirstName());
        log.setAction("User Registration");
        log.setIpAddress(request.getRemoteAddr());
        log.setActorRole(user.getRole());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepo.save(log);

        StandardDTO<User> response = new StandardDTO<>();
        response.setStatusCode(HttpStatus.CREATED.value());
        response.setMessage("User registered successfully");
        response.setData(user);
        response.setMetadata(null);
        return response;
    }

    @PostMapping("/login")
    public ResponseEntity<StandardDTO<Map<String, Object>>> login(@RequestBody LoginRequest loginRequest) {
        AuditLog log = new AuditLog();
        Optional<User> userByEmail = userRepository.findByEmail(loginRequest.getEmail());
        String email = loginRequest.getEmail();

        try {
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
            log.setActorId(userByEmail.map(User::getId).orElse(null));
            log.setActorRole(role);
            log.setMessage("'" + email + "' logged in successfully");
            auditLogRepo.save(log);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", token);
            responseData.put("user", userByEmail);

            StandardDTO<Map<String, Object>> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.OK.value());
            response.setMessage("Login successful");
            response.setData(responseData);
            response.setMetadata(null);

            return ResponseEntity.ok(response);

        } catch (AuthenticationException ex) {
            log.setAction("Failed Login");
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(request.getRemoteAddr());
            log.setActorId(userByEmail.map(User::getId).orElse(null));
            log.setActorRole(userByEmail.map(User::getRole).orElse("UNKNOWN"));
            log.setMessage("Failed login attempt with email '" + email + "'");
            auditLogRepo.save(log);

            StandardDTO<Map<String, Object>> errorResponse = new StandardDTO<>();
            errorResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());
            errorResponse.setMessage("Invalid email or password");
            errorResponse.setData(null);
            errorResponse.setMetadata(null);

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
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
