package com.footballymba.api.features.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SetupPasswordDto {
    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, max = 30, message = "Пароль должен быть от 6 до 30 символов")
    private String password;
}