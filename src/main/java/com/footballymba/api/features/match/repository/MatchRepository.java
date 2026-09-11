package com.footballymba.api.features.match.repository;

import com.footballymba.api.features.match.entity.Match;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Integer> {
    @Query("SELECT m FROM Match m JOIN m.players p WHERE p.id = :playerId AND m.status = com.footballymba.api.features.match.entity.MatchStatus.FINISHED ORDER BY m.createdAt DESC")
    List<Match> findLastMatchesByPlayerId(@Param("playerId") Integer playerId, Pageable pageable);
}