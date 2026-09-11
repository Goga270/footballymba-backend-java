package com.footballymba.api.features.player.dto;

import com.footballymba.api.features.player.entity.PlayerTier;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PlayerProfileDto {
    private Integer id;
    private String nickname;
    private String fullName;
    private String avatarUrl;
    private PlayerTier tier;
    private StatsBlock stats;
    private List<PlayerMatchHistoryItem> matchHistory;
    private List<PlayerTournamentItem> tournaments;

    @Data
    @Builder
    public static class StatsBlock {
        private StatWithRank matchesPlayed;
        private StatWithRank wins;
        private StatWithRank goals;
        private StatWithRank assists;
    }
}