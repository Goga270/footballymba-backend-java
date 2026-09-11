package com.footballymba.api.features.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    @NotBlank(message = "Никнейм (логин) не может быть пустым")
    private String nickname;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}