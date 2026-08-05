package com.fcolucasvieira.racha_manager.session.mapper;

import com.fcolucasvieira.racha_manager.session.dto.response.MatchDTO;
import com.fcolucasvieira.racha_manager.player.dto.response.PlayerDTO;
import com.fcolucasvieira.racha_manager.session.dto.response.SessionDTO;
import com.fcolucasvieira.racha_manager.session.dto.response.TeamDTO;
import com.fcolucasvieira.racha_manager.session.model.Match;
import com.fcolucasvieira.racha_manager.player.model.Player;
import com.fcolucasvieira.racha_manager.session.model.Session;
import com.fcolucasvieira.racha_manager.session.model.Team;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SessionMapper {
    public SessionDTO toResponse(Session session) {

        // Seleta currentMatch em uma variável
        Match currentMatch = session.getCurrentMatch();

        MatchDTO currentMatchDTO =
                (currentMatch != null)
                        ? toMatchDTO(currentMatch)
                        : null;

            return new SessionDTO(
                    session.getId(),
                    session.hasStarted(),
                    toPlayerDTOList(session.getActivePlayers()),
                    currentMatchDTO,
                    toTeamDTOList(session.getWaitingTeams())
            );
        }

    private List<TeamDTO> toTeamDTOList(List<Team> teams) {
        return teams.stream()
                .map(this::toTeamDTO)
                .toList();
    }

    private List<PlayerDTO> toPlayerDTOList(List<Player> players) {
        return players.stream()
                .map(this::toPlayerDTO)
                .toList();
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
                toPlayerDTOList(team.getPlayers())
        );
    }

    private PlayerDTO toPlayerDTO(Player player){
        return new PlayerDTO(
                player.getId(),
                player.getName()
        );
    }
}