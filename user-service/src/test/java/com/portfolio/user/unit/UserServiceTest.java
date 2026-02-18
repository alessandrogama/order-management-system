package com.portfolio.user.unit;

import com.portfolio.user.domain.enums.UserRole;
import com.portfolio.user.domain.model.User;
import com.portfolio.user.dto.request.LoginRequest;
import com.portfolio.user.dto.request.RegisterRequest;
import com.portfolio.user.dto.response.AuthResponse;
import com.portfolio.user.dto.response.UserResponse;
import com.portfolio.user.exception.EmailAlreadyExistsException;
import com.portfolio.user.exception.UserNotFoundException;
import com.portfolio.user.mapper.UserMapper;
import com.portfolio.user.repository.UserRepository;
import com.portfolio.user.security.JwtService;
import com.portfolio.user.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserServiceImpl Unit Tests")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User buildUser() {
        return User.builder()
                .id("user-123")
                .name("Test User")
                .email("test@test.com")
                .password("encoded-pass")
                .role(UserRole.USER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private UserResponse buildUserResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(),
                user.getRole(), user.getActive(), user.getCreatedAt());
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUser() {
        var request = new RegisterRequest("Test User", "test@test.com", "password123");
        var user = buildUser();
        var response = buildUserResponse(user);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(response);

        AuthResponse result = userService.register(request);

        assertThat(result).isNotNull();
        assertThat(result.token()).isEqualTo("jwt-token");
        assertThat(result.user().email()).isEqualTo("test@test.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
    void shouldThrowWhenEmailExists() {
        var request = new RegisterRequest("Test", "test@test.com", "pass123");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("test@test.com");
    }

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        var request = new LoginRequest("test@test.com", "password123");
        var user = buildUser();
        var response = buildUserResponse(user);

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(userMapper.toResponse(user)).thenReturn(response);

        AuthResponse result = userService.login(request);

        assertThat(result.token()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("Should throw BadCredentialsException for wrong password")
    void shouldThrowForWrongPassword() {
        var request = new LoginRequest("test@test.com", "wrong-pass");
        var user = buildUser();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException when user does not exist")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById("non-existent"))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Should return all users")
    void shouldReturnAllUsers() {
        var user = buildUser();
        var response = buildUserResponse(user);

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toResponse(user)).thenReturn(response);

        List<UserResponse> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("test@test.com");
    }

    @Test
    @DisplayName("Should deactivate user on delete")
    void shouldDeactivateUserOnDelete() {
        var user = buildUser();
        when(userRepository.findById("user-123")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        userService.delete("user-123");

        assertThat(user.getActive()).isFalse();
        verify(userRepository).save(user);
    }
}
