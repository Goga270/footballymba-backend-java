package com.footballymba.api.features.auth.repository;

import com.footballymba.api.features.auth.entity.MagicToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MagicTokenRepository extends JpaRepository<MagicToken, Integer> {
    Optional<MagicToken> findByToken(String token);
    Optional<MagicToken> findByPlayerId(Integer playerId); 
    void deleteByPlayerId(Integer playerId);
}