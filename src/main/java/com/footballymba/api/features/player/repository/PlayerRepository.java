package com.footballymba.api.features.player.repository;

import com.footballymba.api.features.player.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Integer> {
    Optional<Player> findByNickname(String nickname);
    Optional<Player> findByTgId(Long tgId);
    boolean existsByNickname(String nickname);

    @Query(value = "SELECT COUNT(mp.match_id) FROM match_players mp " +
                   "JOIN matches m ON mp.match_id = m.id " +
                   "WHERE mp.player_id = :playerId AND m.status = 'FINISHED'", nativeQuery = true)
    int countMatchesPlayed(@Param("playerId") Integer playerId);

    @Query(value = "SELECT ranking FROM (" +
                   "  SELECT player_id, RANK() OVER (ORDER BY COUNT(mp.match_id) DESC) as ranking " +
                   "  FROM match_players mp " +
                   "  JOIN matches m ON mp.match_id = m.id " +
                   "  WHERE m.status = 'FINISHED' " +
                   "  GROUP BY player_id" +
                   ") r WHERE player_id = :playerId", nativeQuery = true)
    Integer getMatchesPlayedRank(@Param("playerId") Integer playerId);

    @Query(value = "SELECT COUNT(a.id) FROM actions a " +
                   "JOIN matches m ON a.match_id = m.id " +
                   "WHERE a.player_id = :playerId AND a.action_type = 'goal' AND m.status = 'FINISHED'", nativeQuery = true)
    int countTotalGoals(@Param("playerId") Integer playerId);

    @Query(value = "SELECT ranking FROM (" +
                   "  SELECT player_id, RANK() OVER (ORDER BY COUNT(a.id) DESC) as ranking " +
                   "  FROM actions a " +
                   "  JOIN matches m ON a.match_id = m.id " +
                   "  WHERE a.action_type = 'goal' AND m.status = 'FINISHED' " +
                   "  GROUP BY player_id" +
                   ") r WHERE player_id = :playerId", nativeQuery = true)
    Integer getGoalsRank(@Param("playerId") Integer playerId);

    @Query(value = "SELECT COUNT(a.id) FROM actions a " +
                   "JOIN matches m ON a.match_id = m.id " +
                   "WHERE a.player_id = :playerId AND a.action_type = 'assist' AND m.status = 'FINISHED'", nativeQuery = true)
    int countTotalAssists(@Param("playerId") Integer playerId);

    @Query(value = "SELECT ranking FROM (" +
                   "  SELECT player_id, RANK() OVER (ORDER BY COUNT(a.id) DESC) as ranking " +
                   "  FROM actions a " +
                   "  JOIN matches m ON a.match_id = m.id " +
                   "  WHERE a.action_type = 'assist' AND m.status = 'FINISHED' " +
                   "  GROUP BY player_id" +
                   ") r WHERE player_id = :playerId", nativeQuery = true)
    Integer getAssistsRank(@Param("playerId") Integer playerId);

    @Query(value = "SELECT COUNT(id) FROM actions " +
                   "WHERE match_id = :matchId AND player_id = :playerId AND action_type = :actionType", nativeQuery = true)
    int countActionsInMatch(@Param("matchId") Integer matchId, @Param("playerId") Integer playerId, @Param("actionType") String actionType);

    @Query(value = "SELECT COUNT(*) FROM match_players mp " +
                   "JOIN matches m ON mp.match_id = m.id " +
                   "JOIN draft_states ds ON ds.match_id = m.id " +
                   "WHERE mp.player_id = :playerId " +
                   "  AND m.status = 'FINISHED' " +
                   "  AND ( " +
                   "    (mp.player_id = ANY(string_to_array(ds.team_a, ',')::int[]) AND m.score_a > m.score_b) " +
                   "    OR " +
                   "    (mp.player_id = ANY(string_to_array(ds.team_b, ',')::int[]) AND m.score_b > m.score_a) " +
                   "  )", nativeQuery = true)
    int countWinsByPlayerId(@Param("playerId") Integer playerId);

    @Query(value = "SELECT ranking FROM ( " +
                   "  SELECT p.id as player_id, RANK() OVER (ORDER BY COUNT(m.id) DESC) as ranking " +
                   "  FROM players p " +
                   "  LEFT JOIN match_players mp ON p.id = mp.player_id " +
                   "  LEFT JOIN matches m ON mp.match_id = m.id AND m.status = 'FINISHED' " +
                   "  LEFT JOIN draft_states ds ON ds.match_id = m.id " +
                   "  WHERE m.id IS NULL OR ( " +
                   "    (p.id = ANY(string_to_array(ds.team_a, ',')::int[]) AND m.score_a > m.score_b) " +
                   "    OR " +
                   "    (p.id = ANY(string_to_array(ds.team_b, ',')::int[]) AND m.score_b > m.score_a) " +
                   "  ) " +
                   "  GROUP BY p.id " +
                   ") r WHERE player_id = :playerId", nativeQuery = true)
    Integer getWinsRank(@Param("playerId") Integer playerId);
}