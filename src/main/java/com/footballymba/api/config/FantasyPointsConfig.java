package com.footballymba.api.config;

import com.footballymba.api.features.player.entity.PlayerTier;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "fantasy-points")
@Data
public class FantasyPointsConfig {
    private int win;
    private int draw;
    private int play;
    // private int ownGoal;
    // private int yellowCard;
    // private int redCard;
    
    private Map<PlayerTier, TierConfig> tiers = new HashMap<>();

    @Data
    public static class TierConfig {
        private int goals;
        private int assists;
        private int cleanSheet;
    }
}