package com.footballymba.api.features.player.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatWithRank {
    private int value;
    private int rank;
}
