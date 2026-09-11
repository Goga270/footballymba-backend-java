package com.footballymba.api.features.player.dto;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class PlayerMatchHistoryItem {
    private Integer matchId;
    private String matchName;
    private String date;
    private String myTeamName;
    private String opponentTeamName;
    private int myTeamScore;
    private int opponentTeamScore;
    private boolean isWin;
    private int goals;              
    private int assists;
    private int fantasyPoints;
}
