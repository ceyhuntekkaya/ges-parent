package com.genixo.ges.security;

import com.genixo.ges.auth.repo.UserAccountRepository;
import java.util.UUID;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AuthUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccounts;

    public AuthUserDetailsService(UserAccountRepository userAccounts) {
        this.userAccounts = userAccounts;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userAccounts.findByEmailIgnoreCase(username)
            .map(AuthUserPrincipal::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public AuthUserPrincipal loadById(UUID userId) {
        return userAccounts.findById(userId)
            .map(AuthUserPrincipal::new)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}

