package com.footballymba.api.features.match.entity;

import com.footballymba.api.features.player.entity.Player;
import com.footballymba.api.features.tournament.entity.TournamentPairing;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "matches")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "captain_a_id", nullable = false)
    private Long captainAId;

    @Column(name = "captain_b_id", nullable = false)
    private Long captainBId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING) // Hibernate автоматически будет писать в БД строки 'CREATED', 'FINISHED'
    @Builder.Default
    private MatchStatus status = MatchStatus.CREATED;

    @Column(name = "is_return_match")
    @Builder.Default
    private Boolean isReturnMatch = false;

    @Column(name = "score_a")
    private Integer scoreA;

    @Column(name = "score_b")
    private Integer scoreB;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "match_players",
        joinColumns = @JoinColumn(name = "match_id"),
        inverseJoinColumns = @JoinColumn(name = "player_id")
    )
    @Builder.Default
    private List<Player> players = new ArrayList<>();

    // Связь 1-к-1 с состоянием драфта этого матча
    @OneToOne(mappedBy = "match", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private DraftState draft;

    // Связь с турнирной сеткой
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_pairing_id")
    private TournamentPairing pairing;
}