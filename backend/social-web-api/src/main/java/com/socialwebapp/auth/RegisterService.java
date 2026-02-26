package com.socialwebapp.auth;

import com.socialwebapp.auth.data.UserEntity;
import com.socialwebapp.auth.data.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service responsible for registering new users.
 */
@Service
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user with unique email and hashed password.
     *
     * @param email    unique user email
     * @param password raw password to hash (BCrypt)
     * @return created user entity
     * @throws IllegalArgumentException if email already exists
     */
    @Transactional
    public UserEntity register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        String hash = passwordEncoder.encode(password);
        UserEntity entity = new UserEntity(email, hash);

        return userRepository.save(entity);
    }
}