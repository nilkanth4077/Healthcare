package com.e_commerce;

import com.e_commerce.entity.AuditLog;
import com.e_commerce.entity.User;
import com.e_commerce.repository.AuditLogRepo;
import com.e_commerce.repository.UserRepo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
public class HealthCareApplication {

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuditLogRepo auditLogRepo;

	@Value("${admin.default.email}")
	private String defaultAdminEmail;

	@Value("${admin.default.password}")
	private String defaultAdminPassword;

	public static void main(String[] args) {
		SpringApplication.run(HealthCareApplication.class, args);
	}

	@Bean
	public CommandLineRunner initAdmin() {
		return args -> {
			boolean adminExists = userRepo.existsByRole("ADMIN");
			AuditLog log = new AuditLog();

			if (!adminExists) {
				User admin = new User();
				admin.setFirstName("Nilkanth");
				admin.setLastName("Patel");
				admin.setEmail(defaultAdminEmail);
				admin.setPassword(passwordEncoder.encode(defaultAdminPassword));
				admin.setMobile("9316277803");
				admin.setRole("ADMIN");

				userRepo.save(admin);

				log.setActorId(admin.getId());
				log.setMessage("Name: " + admin.getFirstName() + " " + admin.getRole());
				log.setAction("New Default Admin Creation");
				log.setActorRole(admin.getRole());
				log.setTimestamp(LocalDateTime.now());
				log.setIpAddress("Admin Machine");

				auditLogRepo.save(log);

				System.out.println("✅ Default admin created: admin123@example.com / passcode");
			} else {
				System.out.println("ℹ️ Admin already exists.");
			}
		};
	}
}
