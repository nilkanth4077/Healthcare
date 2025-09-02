package com.healthcare.service;

import com.healthcare.dto.StandardDTO;
import com.healthcare.dto.UpdateDoctorRequest;
import com.healthcare.dto.UpdateDoctorResponse;
import com.healthcare.entity.AuditLog;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Specialization;
import com.healthcare.entity.User;
import com.healthcare.exception.UserException;
import com.healthcare.repository.AuditLogRepo;
import com.healthcare.repository.DoctorRepo;
import com.healthcare.repository.SpecializationRepo;
import com.healthcare.repository.UserRepo;
import com.healthcare.util.FileUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    private final UserService userService;
    private final FileUtil fileUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public StandardDTO<?> registerDoctor(String firstName, String lastName, String email, String password, String confirmPassword, String mobile, String specialization, MultipartFile documentFile) throws Exception {

        if (documentFile != null && !fileUtil.getFileExtension(documentFile).equalsIgnoreCase("pdf")) {
            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("Only PDF file allowed");
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

        long maxFileSize = 2 * 1024 * 1024;

        if (documentFile != null && documentFile.getSize() > maxFileSize) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Signup failed: File size exceeded for " + email);
            log.setAction("Doctor Registration Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("UNKNOWN");
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);

            StandardDTO<User> response = new StandardDTO<>();
            response.setStatusCode(HttpStatus.BAD_REQUEST.value());
            response.setMessage("File size exceeds the limit. Only files up to 2MB are allowed.");
            response.setData(null);
            response.setMetadata(null);
            return response;
        }

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
        user.setActive(true);

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
        log.setIpAddress(request.getRemoteAddr());

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

    public void deleteDoctor(Long id, String token) throws UserException {

        Doctor doc = doctorRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with id: " + id));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (!doctorRepository.existsById(id)) {
            AuditLog log = new AuditLog();
            log.setActorId(null);
            log.setMessage("Deletion failed for doctor '" + doc.getUser().getEmail() + "'");
            log.setAction("Doctor Deletion Failed");
            log.setIpAddress(request.getRemoteAddr());
            log.setActorRole("ADMIN");
            log.setActorId(sessionUser.getId());
            log.setTimestamp(LocalDateTime.now());
            auditLogRepository.save(log);
            throw new RuntimeException("Doctor not found with id " + id);
        }

        AuditLog log = new AuditLog();
        log.setActorId(null);
        log.setMessage("Deletion successful for email '" + doc.getUser().getEmail() + "'");
        log.setAction("Doctor Deletion Successful");
        log.setIpAddress(request.getRemoteAddr());
        log.setActorRole("ADMIN");
        log.setActorId(sessionUser.getId());
        log.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(log);

        doctorRepository.deleteById(id);
        userRepository.deleteById(doc.getUser().getId());

    }

    public List<Doctor> getDoctorBySpeciality(String speciality) {
        return doctorRepository.findVerifiedDoctorsBySpecialization(speciality);
    }

    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    public Map<String, Object> findDoctorByUserId(Long userId) {
        Doctor doc = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with provided user Id"));

        return Map.of(
                "doctorId", doc.getId(),
                "userId", doc.getUser().getId(),
                "speciality", doc.getSpecialization(),
                "verificationStatus", doc.getVerificationStatus(),
                "email", doc.getUser().getEmail(),
                "firstName", doc.getUser().getFirstName(),
                "lastName", doc.getUser().getLastName(),
                "mobile", doc.getUser().getMobile()
        );
    }

    public UpdateDoctorResponse updateDoctorDetails(Long doctorId, String firstName, String lastName,
                                                    String email, String verificationStatus,
                                                    String specialization, String mobile, Boolean active,
                                                    MultipartFile docFile,
                                                    String token) throws UserException, IOException {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with id: " + doctorId));
        User sessionUser = userService.getProfileByToken(token)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with provided token"));

        if (docFile != null && !fileUtil.getFileExtension(docFile).equalsIgnoreCase("pdf")) {
            throw new IllegalArgumentException("Only PDF files allowed");
        }

        long maxFileSize = 2 * 1024 * 1024;

        if (docFile != null && docFile.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeded (Maximum size: 2MB)");
        }

        String role = sessionUser.getRole();

        if ("DOCTOR".equalsIgnoreCase(role)) {
            if (!doctor.getUser().getEmail().equals(sessionUser.getEmail())) {
                throw new SecurityException("You are not allowed to update other doctor's details");
            }
        } else if ("ADMIN".equalsIgnoreCase(role)) {

        } else {
            throw new SecurityException("You are not allowed to update doctor details");
        }

        if (specialization != null && !specialization.isBlank()) {
            doctor.setSpecialization(specialization);
        }
        if (email != null && !email.isBlank()) {
            doctor.getUser().setEmail(email);
        }
        if (mobile != null && !mobile.isBlank()) {
            doctor.getUser().setMobile(mobile);
        }
        if (firstName != null && !firstName.isBlank()) {
            doctor.getUser().setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            doctor.getUser().setLastName(lastName);
        }
        if (verificationStatus != null && !verificationStatus.isBlank()) {
            doctor.setVerificationStatus(verificationStatus);
        }
        if (docFile != null && !docFile.isEmpty()) {
            doctor.setDocument(docFile.getBytes());
        }
        if (active != null) {
            doctor.getUser().setActive(active);
        }

        doctorRepository.save(doctor);

        UpdateDoctorResponse res = new UpdateDoctorResponse();
        res.setVerificationStatus(doctor.getVerificationStatus());
        res.setDoctorId(doctor.getId());
        res.setEmail(doctor.getUser().getEmail());
        res.setMobile(doctor.getUser().getMobile());
        res.setFileSize(doctor.getDocument().length);
        res.setFirstName(doctor.getUser().getFirstName());
        res.setLastName(doctor.getUser().getLastName());
        res.setSpecialization(doctor.getSpecialization());

        return res;
    }
}