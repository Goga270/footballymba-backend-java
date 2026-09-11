package com.footballymba.api.features.auth.controller;

import com.footballymba.api.exception.UnauthorizedException;
import com.footballymba.api.features.auth.dto.*;
import com.footballymba.api.features.auth.service.AuthService;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${telegram.bot.internal-secret}")
    private String botInternalSecret;

    @Value("${app.security.cookie-secure}")
    private boolean cookieSecure;

    /**
     * Создает безопасную куку для отправки на клиент.
     */
    private ResponseCookie createHttpOnlyCookie(String refreshTokenString) {
        return ResponseCookie.from("refreshToken", refreshTokenString)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Lax")
                .build();
    }

    /**
     * Создает пустую куку с нулевым временем жизни для очистки браузера при логауте.
     */
    private ResponseCookie createEmptyCookie() {
        return ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/v1/auth")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }

    /**
     * ЭНДПОИНТ ДЛЯ БОТА: Создание ссылки.
     * Защищен проверкой заголовка X-Internal-Secret.
     */
    @PostMapping("/magic-link")
    public ResponseEntity<MagicLinkResponseDto> createMagicLink(
            @RequestHeader("X-Internal-Secret") String clientSecret,
            @Valid @RequestBody MagicLinkRequestDto request) {

        if (!botInternalSecret.equals(clientSecret)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MagicLinkResponseDto response = authService.generateMagicLink(request);
        return ResponseEntity.ok(response);
    }

    /**
     * ЭНДПОИНТ ДЛЯ ФРОНТА: Вход по токену из ссылки.
     */
    @PostMapping("/magic-login")
    public ResponseEntity<LoginResponseDto> magicLogin(@RequestParam String token, HttpServletResponse response) {
        TokenResponseDto tokenDto = authService.loginViaMagicLink(token);

        ResponseCookie cookie = createHttpOnlyCookie(tokenDto.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        LoginResponseDto loginResponse = new LoginResponseDto(
                tokenDto.getAccessToken(),
                tokenDto.getNickname(),
                tokenDto.isPasswordSet()
        );

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * ЭНДПОИНТ ДЛЯ ФРОНТА: Обычный вход по никнейму и паролю.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request, HttpServletResponse response) {
        TokenResponseDto tokenDto = authService.loginViaCredentials(request);

        ResponseCookie cookie = createHttpOnlyCookie(tokenDto.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        LoginResponseDto loginResponse = new LoginResponseDto(
                tokenDto.getAccessToken(),
                tokenDto.getNickname(),
                tokenDto.isPasswordSet()
        );

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * ЭНДПОИНТ SILENT REFRESH: Бесшумное обновление сессии.
     * Браузер автоматически пришлет куку с refreshToken. Мы выдадим новый Access Token и обновим куку.
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(
            @CookieValue(name = "refreshToken", required = false) String oldRefreshToken,
            HttpServletResponse response) {

        if (oldRefreshToken == null) {
            throw new UnauthorizedException("Сессия отсутствует. Войдите в аккаунт.");
        }

        TokenResponseDto tokenDto = authService.refresh(oldRefreshToken);

        ResponseCookie cookie = createHttpOnlyCookie(tokenDto.getRefreshToken());
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        LoginResponseDto loginResponse = new LoginResponseDto(
                tokenDto.getAccessToken(),
                tokenDto.getNickname(),
                tokenDto.isPasswordSet()
        );

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * ЭНДПОИНТ ВЫХОДА (LOGOUT): Удаляем токен из БД и стираем куку в браузере.
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        authService.logout(refreshToken);

        ResponseCookie emptyCookie = createEmptyCookie();
        response.addHeader(HttpHeaders.SET_COOKIE, emptyCookie.toString());

        return ResponseEntity.noContent().build();
    }
}