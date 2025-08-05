package com.healthcare.service;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Specialization;
import com.healthcare.entity.User;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.DoctorRepo;
import com.healthcare.repository.SpecializationRepo;
import com.healthcare.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final DoctorRepo doctorRepository;
    private final UserRepo userRepository;
    private final AuditLogRepo auditLogRepository;
    private final SpecializationRepo specializationRepository;
    private final HttpServletRequest request;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public StandardDTO<?> registerDoctor(String firstName, String lastName, String email, String password, String confirmPassword, String mobile, String specialization, MultipartFile documentFile) throws Exception {

        if (!password.equals(confirmPassword)) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Signup failed: Passwords do not match for " + email);
            log.setAction("Doctor Registration Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("UNKNOWN");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);

            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Passwords do not match");
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

        if (userRepository.existsByEmail(email)) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Signup failed: Email already exists - " + email);
            log.setAction("Doctor Registration Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("UNKNOWN");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);

            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.CONFLICT.value());
            response.setMessage("Email already exists: " + email);
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setMobile(mobile);
        user.setRole("DOCTOR");

        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setSpecialization(specialization);
        doctor.setVerificationStatus("Pending");
        doctor.setUser(user);
        doctor.setDocument(documentFile.getBytes());

        doctor = doctorRepository.save(doctor);

        AuditLog log = new AuditLog();
        log.setActorId(user.getId());
        log.setActorRole("DOCTOR");
        log.setTargetEntity("Doctor");
        log.setTargetId(doctor.getId());
        log.setAction("REGISTER");
        log.setMessage("Doctor registered with email: " + user.getEmail());
        log.setIpAddress("UNKNOWN");

        auditLogRepository.save(log);

        Map<String, Object> res = new HashMap<>();
        res.put("user", user);
        res.put("doctorId", doctor.getId());
        res.put("verificationStatus", doctor.getVerificationStatus());
        res.put("specialization", doctor.getSpecialization());
        res.put("documentSize", doctor.getDocument().length);

        return new StandardDTO<>(201, "Doctor registered successfully", res, null);
    }

    public Specialization addSpecialization(Specialization specialization) {
        return specializationRepository.save(specialization);
    }

    public List<Specialization> getAllSpecializations() {
        return specializationRepository.findAll();
    }

    public Specialization updateSpecialization(Long id, Specialization updated) {
        Optional<Specialization> optional = specializationRepository.findById(id);
        if (optional.isPresent()) {
            Specialization spec = optional.get();
            spec.setSpecialization(updated.getSpecialization());
            return specializationRepository.save(spec);
        } else {
            throw new RuntimeException("Specialization not found with id " + id);
        }
    }

    public void deleteSpecialization(Long id) {
        if (!specializationRepository.existsById(id)) {
            throw new RuntimeException("Specialization not found with id " + id);
        }
        specializationRepository.deleteById(id);
    }

    public List<Doctor> getDoctorBySpeciality(String speciality) {
        return doctorRepository.findBySpecializationIgnoreCase(speciality);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }
}