package com.footballymba.api.features.tournament.repository;

import com.footballymba.api.features.tournament.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TournamentRepository extends JpaRepository<Tournament, Integer> {

    // Достаем турниры, в которых у игрока есть хотя бы один сыгранный матч через турнирную сетку (pairing)
    @Query("SELECT DISTINCT t FROM Tournament t " +
           "JOIN TournamentPairing tp ON tp.tournament.id = t.id " +
           "JOIN tp.matches m " +
           "JOIN m.players p " +
           "WHERE p.id = :playerId")
    List<Tournament> findTournamentsByPlayerId(@Param("playerId") Integer playerId);
}