package com.fcolucasvieira.racha_manager.mapper;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.dto.MatchResponse;
import com.fcolucasvieira.racha_manager.dto.SessionActivePlayerResponse;
import com.fcolucasvieira.racha_manager.dto.SessionTeamResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionMapper {

    public List<SessionTeamResponse> toTeamResponseList(List<Team> teams) {
        return teams.stream()
                .map(this::toTeamResponse)
                .toList();
    }

    public SessionTeamResponse toTeamResponse(Team team) {
        return new SessionTeamResponse(
                team.getNumber(),
                toPlayerNames(team.getPlayers())
        );
    }

    public List<SessionActivePlayerResponse> toActivePlayerResponseList(List<PlayerEntity> activePlayers) {
        return activePlayers.stream()
                .map(this::toActivePlayerResponse)
                .toList();
    }

    public SessionActivePlayerResponse toActivePlayerResponse(PlayerEntity player) {
        return new SessionActivePlayerResponse(
                player.getId(),
                player.getName()
        );
    }

    public List<String> toPlayerNames(List<PlayerEntity> players) {
        return players.stream()
                .map(PlayerEntity::getName)
                .toList();
    }

    public MatchResponse toMatchResponse(Match match) {
        return new MatchResponse(
                toTeamResponse(match.getTeamA()),
                toTeamResponse(match.getTeamB())
        );
    }
}