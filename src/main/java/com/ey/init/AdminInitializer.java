package com.ey.init;
 
import com.ey.entity.User;
import com.ey.entity.Role;
import com.ey.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.Optional;
 
@Component
public class AdminInitializer implements ApplicationRunner {
 
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminPassword;
 
    public AdminInitializer(UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            @Value("${app.admin.password:admin123}") String adminPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminPassword = adminPassword;
    }
 
    @Override
    public void run(ApplicationArguments args) {
        Optional<User> existing = userRepository.findByEmail("admin@gmail.com");
        if (existing.isEmpty()) {
            User admin = new User();
            admin.setName("Administrator");
            admin.setEmail("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            userRepository.save(admin);
        }
    }
}