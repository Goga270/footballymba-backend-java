package com.footballymba.api.features.tournament.entity;

import com.footballymba.api.features.match.entity.Match;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tournament_pairings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentPairing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "slot_id")
    private Integer slotId;

    @Column(nullable = false)
    private String stage;

    @Column(name = "captain_a_id")
    private Long captainAId;

    @Column(name = "captain_b_id")
    private Long captainBId;

    @Column(name = "is_completed")
    @Builder.Default
    private Boolean isCompleted = false;

    @Column(name = "manual_score_a")
    private Integer manualScoreA;

    @Column(name = "manual_score_b")
    private Integer manualScoreB;

    @Column(name = "manual_score_text")
    private String manualScoreText;

    // В одной турнирной паре может быть сыграно несколько матчей (например, формат BO2) [2]
    @OneToMany(mappedBy = "pairing", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Match> matches = new ArrayList<>();
}