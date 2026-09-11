package com.footballymba.api.features.player.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlayerTournamentItem {
    private Integer tournamentId;
    private String name;
    private boolean isActive;
    private Integer finalStanding;
}