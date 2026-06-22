package com.rewe.studentstrackingsystem.config;

import com.rewe.studentstrackingsystem.user.entity.Role;
import com.rewe.studentstrackingsystem.user.entity.User;
import com.rewe.studentstrackingsystem.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AdminBootstrapConfig {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner ensureAdminUser() {
        return args -> {
            if (userRepository.findByUsername("admin").isPresent()) {
                return;
            }

            var admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123456789"))
                    .firstName("System")
                    .lastName("Administrator")
                    .email("admin@local")
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Default admin user created: username=admin");
        };
    }
}

