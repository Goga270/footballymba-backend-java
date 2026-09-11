package com.footballymba.api.features.auth.service;

import com.footballymba.api.features.auth.dto.*;
import com.footballymba.api.features.auth.entity.MagicToken;
import com.footballymba.api.features.auth.entity.RefreshToken;
import com.footballymba.api.features.auth.repository.MagicTokenRepository;
import com.footballymba.api.features.player.entity.Player;
import com.footballymba.api.features.player.repository.PlayerRepository;
import com.footballymba.api.features.auth.repository.RefreshTokenRepository;
import com.footballymba.api.exception.ResourceNotFoundException;
import com.footballymba.api.exception.UnauthorizedException;
import com.footballymba.api.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PlayerRepository playerRepository;
    private final MagicTokenRepository magicTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${app.security.jwt.expiration-ms}")
    private long accessExpirationMs;

    @Value("${app.security.refresh-expiration-days}")
    private long refreshExpirationDays;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Создает и сохраняет Refresh-токен в базе данных.
     */
    private RefreshToken createRefreshToken(Player player) {
        String tokenString = UUID.randomUUID().toString();
        RefreshToken refreshToken = RefreshToken.builder()
                .player(player)
                .token(tokenString)
                .expiresAt(LocalDateTime.now().plusDays(refreshExpirationDays))
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    /**
     * ФЛОУ БОТА: Генерация одноразовой ссылки. Вызывается по запросу из Python-бота.
     */
    @Transactional
    public MagicLinkResponseDto generateMagicLink(MagicLinkRequestDto request) {
        Player player = playerRepository.findByTgId(request.getTgId())
                .orElseThrow(() -> new ResourceNotFoundException("Игрок с Telegram ID " + request.getTgId() + " не зарегистрирован в боте"));

        MagicToken magicToken = magicTokenRepository.findByPlayerId(player.getId())
                .orElseGet(() -> MagicToken.builder().player(player).build());

        String tokenString = UUID.randomUUID().toString();
        
        magicToken.setToken(tokenString);
        magicToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        magicTokenRepository.save(magicToken);

        String loginUrl = String.format("%s/auth/magic?token=%s", frontendUrl, tokenString);

        return new MagicLinkResponseDto(tokenString, loginUrl);
    }

    /**
     * ФЛОУ САЙТА: Вход по ссылке из Телеграма. Вызывается по запросу от Фронта.
     */
    @Transactional
    public TokenResponseDto loginViaMagicLink(String tokenString) {
        MagicToken magicToken = magicTokenRepository.findByToken(tokenString)
                .orElseThrow(() -> new UnauthorizedException("Недействительный или уже использованный токен входа"));

        if (magicToken.isExpired()) {
            magicTokenRepository.delete(magicToken);
            throw new UnauthorizedException("Срок действия ссылки истек. Запросите новую ссылку в боте.");
        }

        Player player = magicToken.getPlayer();
        magicTokenRepository.delete(magicToken);
        
        String accessToken = jwtTokenProvider.generateToken(player.getNickname());
        RefreshToken refreshToken = createRefreshToken(player);

        boolean isPasswordSet = player.getPasswordHash() != null;

        return new TokenResponseDto(accessToken, refreshToken.getToken(), player.getNickname(), isPasswordSet);
    }

    /**
     * ФЛОУ САЙТА: Классический вход по логину (никнейму) и паролю.
     */
    @Transactional
    public TokenResponseDto loginViaCredentials(LoginRequestDto request) {
        Player player = playerRepository.findByNickname(request.getNickname())
                .orElseThrow(() -> new UnauthorizedException("Неверный никнейм или пароль"));

        if (player.getPasswordHash() == null) {
            throw new UnauthorizedException("Пароль для вашего аккаунта еще не задан. Пожалуйста, зайдите на сайт по ссылке из Telegram-бота, чтобы задать пароль.");
        }

        if (!passwordEncoder.matches(request.getPassword(), player.getPasswordHash())) {
            throw new UnauthorizedException("Неверный никнейм или пароль");
        }

        String accessToken = jwtTokenProvider.generateToken(player.getNickname());
        RefreshToken refreshToken = createRefreshToken(player);

        return new TokenResponseDto(accessToken, refreshToken.getToken(), player.getNickname(), true);
    }

    /**
     * ФЛОУ ОБНОВЛЕНИЯ: Ротация токенов (Silent Refresh).
     * Проверяет старый Refresh-токен, удаляет его и выдает новые Access и Refresh токены.
     */
    @Transactional
    public TokenResponseDto refresh(String oldTokenString) {
        RefreshToken oldRefreshToken = refreshTokenRepository.findByToken(oldTokenString)
                .orElseThrow(() -> new UnauthorizedException("Невалидная сессия. Войдите заново."));

        if (oldRefreshToken.isExpired()) {
            refreshTokenRepository.delete(oldRefreshToken);
            throw new UnauthorizedException("Сессия истекла. Войдите заново.");
        }

        Player player = oldRefreshToken.getPlayer();

        refreshTokenRepository.delete(oldRefreshToken);

        String newAccessToken = jwtTokenProvider.generateToken(player.getNickname());
        RefreshToken newRefreshToken = createRefreshToken(player);

        return new TokenResponseDto(newAccessToken, newRefreshToken.getToken(), player.getNickname(), true);
    }

    /**
     * ФЛОУ ВЫХОДА: Удаление сессии из БД.
     */
    @Transactional
    public void logout(String tokenString) {
        if (tokenString != null) {
            refreshTokenRepository.deleteByToken(tokenString);
        }
    }
}