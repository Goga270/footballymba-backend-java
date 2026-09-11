package com.footballymba.api.features.player.controller;

import com.footballymba.api.features.player.dto.ChangePasswordDto;
import com.footballymba.api.features.player.dto.PlayerProfileDto;
import com.footballymba.api.features.player.dto.SetupPasswordDto;
import com.footballymba.api.features.player.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    /**
     * ЭНДПОИНТ: Первичная установка пароля при онбординге.
     * Защищен JWT токеном.
     */
    @PostMapping("/me/setup-password")
    public ResponseEntity<Void> setupPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SetupPasswordDto requestDto) {

        playerService.setupPassword(userDetails.getUsername(), requestDto);
        return ResponseEntity.noContent().build(); // Возвращаем 204 No Content
    }

    /**
     * ЭНДПОИНТ: Смена существующего пароля.
     * Защищен JWT токеном.
     */
    @PostMapping("/me/change-password")
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordDto requestDto) {

        playerService.changePassword(userDetails.getUsername(), requestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<PlayerProfileDto> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {

        PlayerProfileDto profile = playerService.getPlayerProfile(userDetails.getUsername());
        return ResponseEntity.ok(profile);
    }
}