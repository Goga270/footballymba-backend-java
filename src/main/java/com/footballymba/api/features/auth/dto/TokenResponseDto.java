package com.footballymba.api.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenResponseDto {
    private String accessToken;
    private String refreshToken;
    private String nickname;
    private boolean isPasswordSet;
}