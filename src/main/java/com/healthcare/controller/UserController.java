package com.healthcare.controller;

import com.healthcare.dto.StandardDTO;
import com.healthcare.entity.Doctor;
import com.healthcare.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @Autowired
    private DoctorService doctorService;

    @GetMapping("/hello")
    public String hello(){
        return "Hello, User!";
    }

    @GetMapping("/doctor-by-speciality")
    public StandardDTO<List<Map<String, Object>>> getDoctorBySpeciality(@RequestParam("speciality") String speciality) {
        List<Doctor> doctors = doctorService.getDoctorBySpeciality(speciality);

        List<Map<String, Object>> responseList = new ArrayList<>();
        for (Doctor doc : doctors) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", doc.getId());
            map.put("specialization", doc.getSpecialization());
            map.put("verificationStatus", doc.getVerificationStatus());
            map.put("doctor", doc.getUser());
            responseList.add(map);
        }

        return new StandardDTO<>(HttpStatus.OK.value(), "Doctors fetched successfully by speciality", responseList, null);
    }
}
