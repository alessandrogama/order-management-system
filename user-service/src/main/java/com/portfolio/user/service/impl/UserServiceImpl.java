package com.portfolio.user.service.impl;

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
import com.portfolio.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    @Override
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved);

        log.info("User registered successfully: {}", saved.getId());
        return new AuthResponse(token, userMapper.toResponse(saved));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getActive()) {
            throw new BadCredentialsException("Account is deactivated");
        }

        String token = jwtService.generateToken(user);
        log.info("Login successful for user: {}", user.getId());
        return new AuthResponse(token, userMapper.toResponse(user));
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse findById(String id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.setActive(false);
        userRepository.save(user);
        log.info("User {} deactivated", id);
    }
}
