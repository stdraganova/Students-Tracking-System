package com.rewe.studentstrackingsystem.config;

import com.rewe.studentstrackingsystem.user.entity.Role;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminInitializerConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin-password:}")
    private String bootstrapAdminPassword;

    @Bean
    public CommandLineRunner ensureAdminUser() {
        return ignoredArgs -> {
            if (userRepository.findByUsername("admin").isPresent()) {
                return;
            }

            var adminPassword = (bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank())
                    ? UUID.randomUUID().toString()
                    : bootstrapAdminPassword;

            var admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode(adminPassword))
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@local")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created: username=admin, bootstrap secret={}", adminPassword);
        };
    }
}

