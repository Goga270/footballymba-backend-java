package com.footballymba.api.features.match.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "draft_states")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftState {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false, unique = true)
    private Match match;

    @Column(nullable = false)
    @Builder.Default
    private String turn = "a";

    @Column(columnDefinition = "TEXT")
    @Builder.Default
    private String pool = "";

    @Column(name = "team_a", columnDefinition = "TEXT")
    @Builder.Default
    private String teamA = "";

    @Column(name = "team_b", columnDefinition = "TEXT")
    @Builder.Default
    private String teamB = "";

    @Column(name = "rps_winner")
    private String rpsWinner;

    @Column(name = "rps_choice_a")
    private String rpsChoiceA;

    @Column(name = "rps_choice_b")
    private String rpsChoiceB;

    @Column(name = "team_a_color")
    @Builder.Default
    private String teamAColor = "🔴";

    @Column(name = "team_b_color")
    @Builder.Default
    private String teamBColor = "🔵";

    /**
     * Конвертирует строку "1,5,12" (ID игроков через запятую) в список Integer.
     */
    public List<Integer> getTeamList(String side) {
        String raw = "a".equalsIgnoreCase(side) ? this.teamA : this.teamB;
        if (raw == null || raw.isBlank()) {
            return Collections.emptyList();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}