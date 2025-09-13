package com.ey.security;
 
import com.ey.entity.User;
import com.ey.repository.UserRepository;
import java.util.Optional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
 
@Service
public class CustomUserDetailsService implements UserDetailsService {
 
    private final UserRepository repo;
 
    public CustomUserDetailsService(UserRepository repo) {
        this.repo = repo;
    }
 
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> opt = repo.findByEmail(email);
        User user = opt.orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
            .password(user.getPassword())
            .authorities("ROLE_" + user.getRole().name())
            .build();
    }
}