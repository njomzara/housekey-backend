package com.housekey.users.application;

import com.housekey.auth.domain.AuthenticatedUser;
import com.housekey.shared.error.ResourceNotFoundException;
import com.housekey.users.api.CurrentUserResponse;
import com.housekey.users.infrastructure.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public CurrentUserResponse getCurrentUser(AuthenticatedUser principal) {
        return userRepository.findById(principal.id())
                .map(CurrentUserResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found."));
    }
}
