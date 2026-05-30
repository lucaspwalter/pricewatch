package com.pricewatch.dto;

import com.pricewatch.model.User;
import java.time.OffsetDateTime;

public record UserResponse(Long id, String email, String phone, OffsetDateTime createdAt) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getPhone(), user.getCreatedAt());
    }
}
