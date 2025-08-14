package com.example.securityapi.security;
import com.example.securityapi.model.Customer;
import com.example.securityapi.repository.CustomerRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import java.util.List;
@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final CustomerRepository customerRepository;
    public CustomUserDetailsService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Customer c = customerRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        // Map boolean isAdmin -> ROLE_ADMIN / ROLE_USER
        var roles = c.isAdmin() ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
                : List.of(new SimpleGrantedAuthority("ROLE_USER"));
        return new User(c.getUsername(), c.getPassword(), roles);
    }
}
