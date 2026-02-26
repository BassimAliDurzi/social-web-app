package com.socialwebapp.auth;

import com.socialwebapp.auth.data.UserEntity;
import com.socialwebapp.auth.data.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    RegisterService registerService;

    @Test
    void register_saves_user_when_email_is_new() {
        // Arrange
        String email = "new@example.com";
        String rawPassword = "Password123!";
        String hash = "$2a$10$hash";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hash);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        UserEntity created = registerService.register(email, rawPassword);

        // Assert
        assertThat(created.getEmail()).isEqualTo(email);
        assertThat(created.getPasswordHash()).isEqualTo(hash);

        verify(userRepository).existsByEmail(email);
        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(UserEntity.class));
        verifyNoMoreInteractions(userRepository, passwordEncoder);
    }

    @Test
    void register_throws_when_email_already_exists() {
        // Arrange
        String email = "dup@example.com";
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act + Assert
        assertThatThrownBy(() -> registerService.register(email, "whatever"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email already exists");

        verify(userRepository).existsByEmail(email);
        verifyNoInteractions(passwordEncoder);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_hashes_password_before_saving() {
        // Arrange
        String email = "hashcheck@example.com";
        String rawPassword = "Password123!";
        String hash = "$2a$10$hash";

        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hash);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);

        // Act
        registerService.register(email, rawPassword);

        // Assert
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(email);
        assertThat(saved.getPasswordHash()).isEqualTo(hash);
        assertThat(saved.getPasswordHash()).isNotEqualTo(rawPassword);
    }
}