package com.pricewatch.dto;

public final class AuthDtos {
    private AuthDtos() {
    }

    public record RegisterRequest(String email, String phone, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    public record AuthResponse(String token, UserResponse user) {
    }
}
