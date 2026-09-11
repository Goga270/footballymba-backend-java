package com.footballymba.api.features.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MagicLinkRequestDto {
    @NotNull(message = "Telegram ID обязателен")
    private Long tgId;
}