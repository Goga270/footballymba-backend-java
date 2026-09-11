package com.footballymba.api.features.player.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.footballymba.api.features.player.dto.ChangePasswordDto;
import com.footballymba.api.features.player.dto.PlayerProfileDto;
import com.footballymba.api.features.player.dto.SetupPasswordDto;
import com.footballymba.api.features.player.entity.PlayerTier;
import com.footballymba.api.features.player.service.PlayerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PlayerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlayerService playerService;

    /**
     * ТЕСТ: Успешное получение профиля текущего игрока.
     * Мы используем @WithMockUser, чтобы прикинуться авторизованным игроком "george" [1.1.6]
     */
    @Test
    @WithMockUser(username = "george")
    public void testGetMyProfile_Success() throws Exception {
        PlayerProfileDto.StatsBlock statsBlock = PlayerProfileDto.StatsBlock.builder()
                .matchesPlayed(new com.footballymba.api.features.player.dto.StatWithRank(10, 5))
                .wins(new com.footballymba.api.features.player.dto.StatWithRank(6, 4))
                .goals(new com.footballymba.api.features.player.dto.StatWithRank(15, 1))
                .assists(new com.footballymba.api.features.player.dto.StatWithRank(10, 2))
                .build();

        PlayerProfileDto mockProfile = PlayerProfileDto.builder()
                .id(1)
                .nickname("george")
                .fullName("George Best")
                .avatarUrl("http://localhost:8080/avatars/george.png")
                .tier(PlayerTier.S)
                .stats(statsBlock)
                .matchHistory(Collections.emptyList())
                .tournaments(Collections.emptyList())
                .build();

        when(playerService.getPlayerProfile("george")).thenReturn(mockProfile);

        mockMvc.perform(get("/api/v1/players/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nickname").value("george"))
                .andExpect(jsonPath("$.tier").value("S"))
                .andExpect(jsonPath("$.stats.goals.value").value(15))
                .andExpect(jsonPath("$.stats.goals.rank").value(1)); 
    }

    /**
     * ТЕСТ: Отказ в получении профиля, если пользователь не авторизован (нет токена/сессии)
     */
    @Test
    public void testGetMyProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/players/me"))
                .andExpect(status().isForbidden()); 
    }

    /**
     * ТЕСТ: Успешная первичная установка пароля
     */
    @Test
    @WithMockUser(username = "george")
    public void testSetupPassword_Success() throws Exception {
        SetupPasswordDto dto = new SetupPasswordDto();
        dto.setPassword("new_password_123");
        doNothing().when(playerService).setupPassword(any(), any());

        mockMvc.perform(post("/api/v1/players/me/setup-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());
    }

    /**
     * ТЕСТ: Успешная смена существующего пароля
     */
    @Test
    @WithMockUser(username = "george")
    public void testChangePassword_Success() throws Exception {
        ChangePasswordDto dto = new ChangePasswordDto();
        dto.setOldPassword("old_password_123");
        dto.setNewPassword("new_password_123");

        doNothing().when(playerService).changePassword(any(), any());

        mockMvc.perform(post("/api/v1/players/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent()); // Ожидаем 204 No Content
    }
}