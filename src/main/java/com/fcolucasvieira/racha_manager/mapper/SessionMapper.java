package com.fcolucasvieira.racha_manager.mapper;

import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import com.fcolucasvieira.racha_manager.dto.*;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {
    public SessionResponseDTO toResponse(Session session) {

        MatchDTO currentMatchDTO = null;

        // Se existir currentMatch
        if(session.getCurrentMatch() != null) {
            Match currentMatch = session.getCurrentMatch();

            currentMatchDTO = new MatchDTO(
                    toTeamDTO(currentMatch.getTeamA()),
                    toTeamDTO(currentMatch.getTeamB())
            );
        }

            return new SessionResponseDTO(
                    session.getId(),
                    session.hasStarted(),
                    currentMatchDTO,
                    session.getQueue()
                            .stream()
                            .map(this::toTeamDTO)
                            .toList()
            );
        }

    private MatchDTO toMatchDTO(Match match) {
        return new MatchDTO(
                toTeamDTO(match.getTeamA()),
                toTeamDTO(match.getTeamB())
        );
    }

    public TeamDTO toTeamDTO(Team team) {
        return new TeamDTO(
                team.getNumber(),
                team.getPlayers().stream()
                        .map(this::toPlayerDTO)
                        .toList()
        );
    }

    private PlayerDTO toPlayerDTO(PlayerEntity player){
        return new PlayerDTO(
                player.getId(),
                player.getName()
        );
    }
}