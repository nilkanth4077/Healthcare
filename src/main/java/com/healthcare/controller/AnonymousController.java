package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.Doctor;
import com.healthcare.entity.Specialization;
import com.healthcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class AnonymousController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/speciality/all")
    public StandardDTO<?> getAll() {
        List<Specialization> res = doctorService.getAllSpecializations();
        return new StandardDTO<>(HttpStatus.OK.value(), "Speciality added successfully", res, null);
    }

    @GetMapping("/get/doctor")
    public StandardDTO<Map<String, Object>> getDoctorById(@RequestParam("docId") Long docId) {
        Doctor doc = doctorService.getDoctorById(docId)
                .orElseThrow(() -> new UsernameNotFoundException("Doctor not found with provided id"));

        Map<String, Object> res = new HashMap<>();
        res.put("docId", doc.getId());
        res.put("firstName", doc.getUser().getFirstName());
        res.put("lastName", doc.getUser().getLastName());
        res.put("email", doc.getUser().getEmail());
        res.put("mobile", doc.getUser().getMobile());
        res.put("specialization", doc.getSpecialization());
        res.put("verificationStatus", doc.getVerificationStatus());

        if (doc.getDocument() != null) {
            String base64Doc = Base64.getEncoder().encodeToString(doc.getDocument());
            res.put("document", doc.getDocument());
        }

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctor fetched successfully with id: " + docId, res, null);
    }
}
