package com.footballymba.api.features.player.service;

import com.footballymba.api.features.match.entity.Match;
import com.footballymba.api.features.match.repository.MatchRepository;
import com.footballymba.api.features.player.dto.ChangePasswordDto;
import com.footballymba.api.features.player.dto.PlayerMatchHistoryItem;
import com.footballymba.api.features.player.dto.PlayerProfileDto;
import com.footballymba.api.features.player.dto.PlayerProfileDto.StatsBlock;
import com.footballymba.api.features.player.dto.PlayerTournamentItem;
import com.footballymba.api.features.player.dto.SetupPasswordDto;
import com.footballymba.api.features.player.dto.StatWithRank;
import com.footballymba.api.features.player.entity.Player;
import com.footballymba.api.features.player.repository.PlayerRepository;
import com.footballymba.api.features.tournament.repository.TournamentRepository;
import com.footballymba.api.features.tournament.entity.Tournament;
import com.footballymba.api.exception.ResourceNotFoundException;
import com.footballymba.api.exception.UnauthorizedException;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final MatchRepository matchRepository;
    private final TournamentRepository tournamentRepository;
    private final PasswordEncoder passwordEncoder;
    private final FantasyPointsCalculator fantasyPointsCalculator;

    /**
     * БИЗНЕС-ЛОГИКА: Первичная установка пароля при онбординге.
     */
    @Transactional
    public void setupPassword(String nickname, SetupPasswordDto dto) {
        Player player = playerRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResourceNotFoundException("Игрок не найден"));

        if (player.getPasswordHash() != null) {
            throw new IllegalStateException("Пароль для вашего аккаунта уже установлен. Используйте метод смены пароля.");
        }

        player.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        playerRepository.save(player);
    }

    /**
     * БИЗНЕС-ЛОГИКА: Смена существующего пароля.
     */
    @Transactional
    public void changePassword(String nickname, ChangePasswordDto dto) {
        Player player = playerRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResourceNotFoundException("Игрок не найден"));

        if (player.getPasswordHash() == null) {
            throw new IllegalStateException("Пароль еще не задан. Используйте форму первичной установки пароля.");
        }

        if (!passwordEncoder.matches(dto.getOldPassword(), player.getPasswordHash())) {
            throw new UnauthorizedException("Неверно указан старый пароль");
        }

        player.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public PlayerProfileDto getPlayerProfile(String nickname) {
        Player player = playerRepository.findByNickname(nickname)
                .orElseThrow(() -> new ResourceNotFoundException("Игрок не найден"));

        Integer playerId = player.getId();

        int matchesPlayed = playerRepository.countMatchesPlayed(playerId);
        int totalGoals = playerRepository.countTotalGoals(playerId);
        int totalAssists = playerRepository.countTotalAssists(playerId);

        int matchesRank = getSafeRank(playerRepository.getMatchesPlayedRank(playerId));
        int goalsRank = getSafeRank(playerRepository.getGoalsRank(playerId));
        int assistsRank = getSafeRank(playerRepository.getAssistsRank(playerId));

        int totalWins = playerRepository.countWinsByPlayerId(playerId);
        int winsRank = getSafeRank(playerRepository.getWinsRank(playerId));

        StatsBlock statsBlock = StatsBlock.builder()
                .matchesPlayed(new StatWithRank(matchesPlayed, matchesRank))
                .wins(new StatWithRank(totalWins, winsRank))
                .goals(new StatWithRank(totalGoals, goalsRank))
                .assists(new StatWithRank(totalAssists, assistsRank))
                .build();

        List<Match> last5Matches = matchRepository.findLastMatchesByPlayerId(playerId, PageRequest.of(0, 5));
        List<PlayerMatchHistoryItem> matchHistory = last5Matches.stream()
                .map(match -> buildMatchHistoryItem(playerId, player.getTier(), match))
                .collect(Collectors.toList());

        List<Tournament> playerTournaments = tournamentRepository.findTournamentsByPlayerId(playerId);
        List<PlayerTournamentItem> tournaments = playerTournaments.stream()
                .map(t -> PlayerTournamentItem.builder()
                        .tournamentId(t.getId())
                        .name(t.getName())
                        .isActive(t.getIsActive())
                        .finalStanding(null)
                        .build())
                .collect(Collectors.toList());
        
        return PlayerProfileDto.builder()
                .id(player.getId())
                .nickname(player.getNickname())
                .fullName(player.getFullName())
                .avatarUrl(player.getAvatarUrl())
                .tier(player.getTier())
                .stats(statsBlock)
                .matchHistory(matchHistory)
                .tournaments(tournaments)
                .build();
    }

    private int getSafeRank(Integer rank) {
        return rank == null ? -1 : rank;
    }


    private PlayerMatchHistoryItem buildMatchHistoryItem(Integer playerId, com.footballymba.api.features.player.entity.PlayerTier tier, Match match) {
        boolean isInTeamA = match.getDraft().getTeamList("a").contains(playerId);

        String myTeamName = isInTeamA ? "Красные" : "Синие";
        String opponentTeamName = isInTeamA ? "Синие" : "Красные";

        int myScore = isInTeamA ? match.getScoreA() : match.getScoreB();
        int opponentScore = isInTeamA ? match.getScoreB() : match.getScoreA();

        boolean isWin = myScore > opponentScore;
        boolean isDraw = myScore == opponentScore;
        boolean isCleanSheet = opponentScore == 0;

        int goals = playerRepository.countActionsInMatch(match.getId(), playerId, "goal");
        int assists = playerRepository.countActionsInMatch(match.getId(), playerId, "assist");
        int ownGoals = 0;
        int yellowCards = 0;
        int redCards = 0;
        // Когда доработаю бд и все остальное добавим
        // int ownGoals = playerRepository.countActionsInMatch(match.getId(), playerId, "own_goal");
        // int yellowCards = playerRepository.countActionsInMatch(match.getId(), playerId, "yellow_card");
        // int redCards = playerRepository.countActionsInMatch(match.getId(), playerId, "red_card");

        int points = fantasyPointsCalculator.calculate(
                tier, true, isWin, isDraw, isCleanSheet, goals, assists, ownGoals, yellowCards, redCards
        );

        String isoDate = match.getCreatedAt() != null 
                ? match.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE) 
                : null;

        return PlayerMatchHistoryItem.builder()
                .matchId(match.getId())
                .matchName("Матч #" + match.getId())
                .date(isoDate)
                .myTeamName(match.getDraft().getTeamAColor() + " " + myTeamName)
                .opponentTeamName(match.getDraft().getTeamBColor() + " " + opponentTeamName)
                .myTeamScore(myScore)
                .opponentTeamScore(opponentScore)
                .isWin(isWin)
                .goals(goals)
                .assists(assists)
                .fantasyPoints(points)
                .build();
    }
}