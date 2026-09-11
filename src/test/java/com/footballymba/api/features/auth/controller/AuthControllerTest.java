package com.footballymba.api.features.auth.controller;

import jakarta.servlet.http.Cookie;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballymba.api.features.auth.dto.*;
import com.footballymba.api.features.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean; // Новый импорт Spring Boot 3.4+ вместо @MockBean
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.doNothing;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Value("${telegram.bot.internal-secret}")
    private String internalSecret;

    /**
     * ТЕСТ: Успешная генерация ссылки боту при правильном секретном ключе
     */
    @Test
    public void testCreateMagicLink_Success() throws Exception {
        MagicLinkRequestDto requestDto = new MagicLinkRequestDto();
        requestDto.setTgId(12345678L);

        MagicLinkResponseDto mockResponse = new MagicLinkResponseDto(
                "test-uuid-token",
                "http://localhost:3000/auth/callback?token=test-uuid-token"
        );

        // Настраиваем заглушку: "Когда у AuthService вызовут метод generateMagicLink с любым аргументом,
        when(authService.generateMagicLink(any())).thenReturn(mockResponse);
        mockMvc.perform(post("/api/v1/auth/magic-link")
                        .header("X-Internal-Secret", internalSecret)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("test-uuid-token"))
                .andExpect(jsonPath("$.loginUrl").value("http://localhost:3000/auth/callback?token=test-uuid-token"));
    }

    /**
     * ТЕСТ: Отказ в генерации ссылки, если бот прислал неверный секретный ключ
     */
    @Test
    public void testCreateMagicLink_WrongSecret_Unauthorized() throws Exception {
        MagicLinkRequestDto requestDto = new MagicLinkRequestDto();
        requestDto.setTgId(12345678L);

        mockMvc.perform(post("/api/v1/auth/magic-link")
                        .header("X-Internal-Secret", "wrong-secret-key-123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnauthorized());
    }

    /**
     * ТЕСТ: Успешное обновление сессии (Silent Refresh).
     * Мы передаем верную куку, ожидаем новый Access Token и новую куку (Ротация).
     */
    @Test
    public void testRefresh_Success() throws Exception {
        // Подготавливаем виртуальный ответ от сервиса
        TokenResponseDto mockTokenResponse = new TokenResponseDto(
                "new-access-token",
                "new-refresh-token",
                "george",
                true
        );

        when(authService.refresh("valid-refresh-token")).thenReturn(mockTokenResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token"))) 
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.nickname").value("george"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new-refresh-token")));
    }

    /**
     * ТЕСТ: Попытка обновить токен без куки.
     * Ожидаем отказ со статусом 401 Unauthorized.
     */
    @Test
    public void testRefresh_NoCookie_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Сессия отсутствует. Войдите в аккаунт."));
    }

    /**
     * ТЕСТ: Успешный выход из аккаунта (Logout).
     * Ожидаем статус 204 No Content и очищающую куку с Max-Age=0.
     */
    @Test
    public void testLogout_Success() throws Exception {
        doNothing().when(authService).logout("valid-refresh-token");

        mockMvc.perform(post("/api/v1/auth/logout")
                        .cookie(new Cookie("refreshToken", "valid-refresh-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
    }
}