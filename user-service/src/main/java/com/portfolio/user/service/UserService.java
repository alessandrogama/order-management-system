package com.portfolio.user.service;

import com.portfolio.user.dto.request.LoginRequest;
import com.portfolio.user.dto.request.RegisterRequest;
import com.portfolio.user.dto.response.AuthResponse;
import com.portfolio.user.dto.response.UserResponse;

import java.util.List;

public interface UserService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    UserResponse findById(String id);
    List<UserResponse> findAll();
    void delete(String id);
}
