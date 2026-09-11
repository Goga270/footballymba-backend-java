package com.footballymba.api.features.player.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "Старый пароль обязателен")
    private String oldPassword;

    @NotBlank(message = "Новый пароль не может быть пустым")
    @Size(min = 6, max = 30, message = "Новый пароль должен быть от 6 до 30 символов")
    private String newPassword;
}