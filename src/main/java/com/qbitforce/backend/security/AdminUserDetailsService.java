package com.qbitforce.backend.security;

import com.qbitforce.backend.repository.AdminUserRepository;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return adminUserRepository
                .findByUsername(username)
                .or(() -> adminUserRepository.findByEmailIgnoreCase(username))
                .map(admin -> User.builder()
                        .username(admin.getUsername())
                        .password(admin.getPasswordHash())
                        .authorities(List.of(new SimpleGrantedAuthority(admin.getRole())))
                        .disabled(!admin.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
    }
}
