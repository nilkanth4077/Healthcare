package com.healthcare.controller;

import com.healthcare.dto.LoginRequest;
import com.healthcare.dto.LoginResponse;
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
    public ResponseEntity<String> signup(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        AuditLog log = new AuditLog();
        Optional<User> userByEmail = userRepository.findByEmail(loginRequest.getEmail());
        String email = loginRequest.getEmail();

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, loginRequest.getPassword()));

            // Successful login
            String token = jwtUtil.generateToken(authentication.getName(),
                    authentication.getAuthorities().iterator().next().getAuthority());

            String role = authentication.getAuthorities().iterator().next().getAuthority();

            LoginResponse loginResponse = new LoginResponse();
            loginResponse.setToken(token);
            loginResponse.setRole(role);

            log.setAction("Login");
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(request.getRemoteAddr());
            log.setActorId(userByEmail.map(User::getId).orElse(null));
            log.setActorRole(role);
            log.setMessage("'" + email + "'" + " logged in successfully");

            auditLogRepo.save(log);
            return ResponseEntity.ok(loginResponse);

        } catch (AuthenticationException ex) {
            // Failed login
            log.setAction("Failed Login");
            log.setTimestamp(LocalDateTime.now());
            log.setIpAddress(request.getRemoteAddr());
            log.setActorId(userByEmail.map(User::getId).orElse(null));
            log.setActorRole(userByEmail.map(User::getRole).orElse("UNKNOWN"));
            log.setMessage("Failed login attempt with email " + "'" + email + "'");

            auditLogRepo.save(log);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
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
