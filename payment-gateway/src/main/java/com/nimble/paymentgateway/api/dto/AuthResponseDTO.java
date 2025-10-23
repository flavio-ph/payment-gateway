package com.nimble.paymentgateway.api.dto;


public record AuthResponseDTO(
        String accessToken,
        String tokenType,
        long expiresIn
) {
    public AuthResponseDTO(String accessToken) {
        this(accessToken, "Bearer", 3600L); 
    }
}