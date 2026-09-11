package com.footballymba.api.features.player.service;

import com.footballymba.api.config.FantasyPointsConfig;
import com.footballymba.api.features.player.entity.PlayerTier;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FantasyPointsCalculator {
    private final FantasyPointsConfig config;

    /**
     * Рассчитывает фэнтези-очки игрока за конкретный матч на основе его спортивного тира и достижений.
     */
    public int calculate(
            PlayerTier tier,
            boolean participated,
            boolean isWin,
            boolean isDraw,
            boolean isCleanSheet,
            int goals,
            int assists,
            int ownGoals,
            int yellowCards,
            int redCards
    ) {
        if (!participated) {
            return 0;
        }

        int points = 0;

        points += config.getPlay();

        if (isWin) {
            points += config.getWin();
        }

        if (isDraw) {
            points += config.getDraw();
        }

        FantasyPointsConfig.TierConfig tierConfig = config.getTiers().get(tier);

        if (tierConfig != null) {
            if (isCleanSheet) {
                points += tierConfig.getCleanSheet();
            }
            points += goals * tierConfig.getGoals();
            points += assists * tierConfig.getAssists();
        }

        //Штрафы
        // points -= ownGoals * 20;     // -20 очков за каждый автогол
        // points -= yellowCards * 10;  // -10 очков за каждую желтую карточку
        // points -= redCards * 30;     // -30 очков за красную карточку

        return points;
    }
}