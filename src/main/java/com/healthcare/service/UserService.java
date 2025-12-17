package com.healthcare.service;

import com.healthcare.entity.Doctor;
import com.healthcare.repository.UserRepo;
import com.healthcare.util.JwtUtil;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.healthcare.entity.User;
import com.healthcare.exception.UserException;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepo userRepo;
    private final JwtUtil jwtUtil;

    public UserService(UserRepo userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    public User findCustomUserById(Long userId) throws UserException {
        Optional<User> user = userRepo.findById(userId);
        if (user.isPresent()) {
            return user.get();
        }
        throw new UserException("User not found with id: " + userId);
    }

    public Optional<User> getProfileByToken(String token) throws UserException {
        String email = jwtUtil.extractEmail(token);

        Optional<User> user = userRepo.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserException("User not found with email: " + email);
        }
        return user;
    }

    public List<User> getAllUsers(String actualToken) throws UserException {
        List<User> users = userRepo.findAll();

        if(users.isEmpty()) {
            throw new UserException("No user found");
        }

        return users;
    }

    public User getUserById(Long patientId, String actualToken) {

        return userRepo.findById(patientId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with Id: " + patientId));
    }

    public User updateUserDetails(Long patientId, String firstName, String lastName, String email, String mobile, Boolean active, String actualToken) throws UserException {
        User user = userRepo.findById(patientId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + patientId));

        if (email != null && !email.isBlank()) {
            user.setEmail(email);
        }
        if (mobile != null && !mobile.isBlank()) {
            user.setMobile(mobile);
        }
        if (firstName != null && !firstName.isBlank()) {
            user.setFirstName(firstName);
        }
        if (lastName != null && !lastName.isBlank()) {
            user.setLastName(lastName);
        }
        if (active != null) {
            user.setActive(active);
        }

        userRepo.save(user);

        return user;
    }
}
