package com.footballymba.api.security;

import com.footballymba.api.features.player.entity.Player;
import com.footballymba.api.features.player.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final PlayerRepository playerRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        Player player = playerRepository.findByNickname(nickname)
                .orElseThrow(() -> new UsernameNotFoundException("Игрок не найден с никнеймом: " + nickname));

        return new User(
                player.getNickname(), 
                player.getPasswordHash() != null ? player.getPasswordHash() : "", 
                Collections.emptyList()
        );
    }
}