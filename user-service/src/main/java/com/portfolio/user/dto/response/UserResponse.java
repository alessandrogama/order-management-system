package com.portfolio.user.dto.response;

import com.portfolio.user.domain.enums.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
        String id,
        String name,
        String email,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt
) {}
