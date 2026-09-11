package com.footballymba.api.features.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MagicLinkResponseDto {
    private String token;
    private String loginUrl;
}