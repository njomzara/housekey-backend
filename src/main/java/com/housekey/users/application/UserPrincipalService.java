package com.housekey.users.application;

import java.util.Optional;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.users.infrastructure.UserEntity;
import com.housekey.users.infrastructure.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class UserPrincipalService implements UserDetailsService {

    private final UserRepository userRepository;

    public UserPrincipalService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(username)
                .or(() -> userRepository.findByEmailIgnoreCase(username))
                .map(this::toPrincipal)
                .orElseThrow(() -> new UsernameNotFoundException("User not found."));
    }

    public Optional<AuthenticatedUser> loadById(Long id) {
        return userRepository.findById(id).map(this::toPrincipal);
    }

    private AuthenticatedUser toPrincipal(UserEntity user) {
        return new AuthenticatedUser(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPasswordHash(),
                user.getRole(),
                user.isEnabled(),
                user.isLocked());
    }
}
