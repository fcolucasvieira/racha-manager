package com.fcolucasvieira.racha_manager.application.mapper;

import com.fcolucasvieira.racha_manager.application.dto.MatchDTO;
import com.fcolucasvieira.racha_manager.application.dto.PlayerDTO;
import com.fcolucasvieira.racha_manager.application.dto.SessionResponseDTO;
import com.fcolucasvieira.racha_manager.application.dto.TeamDTO;
import com.fcolucasvieira.racha_manager.domain.model.Match;
import com.fcolucasvieira.racha_manager.domain.model.PlayerEntity;
import com.fcolucasvieira.racha_manager.domain.model.Session;
import com.fcolucasvieira.racha_manager.domain.model.Team;
import org.springframework.stereotype.Component;

@Component
public class SessionMapper {
    public SessionResponseDTO toResponse(Session session) {

        // Seleta currentMatch em uma variável
        Match currentMatch = session.getCurrentMatch();

        MatchDTO currentMatchDTO =
                (currentMatch != null)
                        ? toMatchDTO(currentMatch)
                        : null;

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