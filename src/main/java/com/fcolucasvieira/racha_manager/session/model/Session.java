package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.INITIAL_PLAYERS;
import static com.fcolucasvieira.racha_manager.session.rules.RachaRules.INITIAL_TEAMS;

@Getter // Anotação -> Getters necessários
public class Session {
    private final UUID id;
    private final List<Player> activePlayers;
    private List<Team> teams;
    private WaitingQueue waitingQueue;
    private Match currentMatch;
    private boolean initialTeamsCreated;

    public Session() {
        this.id = UUID.randomUUID();
        this.activePlayers = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.waitingQueue = null;
        this.initialTeamsCreated = false;
    }

    public void addPlayer(Player player) {
        if(player == null)
            throw new ValidationException("Player can't be null");

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists)
            throw new ConflictException("Player already in session with Id: " + player.getId());

        activePlayers.add(player);
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null)
            throw new ValidationException("Player Id can't be null");

        boolean activePlayersRemoved = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!activePlayersRemoved)
            throw new NotFoundException("Player not found with Id: " + playerId);

        teams.stream()
                .filter(t -> t.containsPlayer(playerId))
                .findFirst()
                .ifPresent(t -> {
                    t.removePlayerById(playerId);

                    if(!t.hasPlayers() && !isCurrentMatchTeam(t))
                        removeTeam(t);
                });
    }

    public Team createNextTeam() {
        int nextTeamNumber = teams.stream()
                .mapToInt(Team::getNumber)
                .max()
                .orElse(0)
                + 1;

        return new Team(nextTeamNumber);
    }

    public void addTeam(Team team) {
        if(team == null)
            throw new ValidationException("Team can't be null");

        if(!hasStarted())
            throw new ConflictException("Team can't be added while session not started");

        if(teams.contains(team))
            throw new ConflictException("Team already exists with Number: " + team.getNumber());

        teams.add(team);

        waitingQueue.add(team);
    }

    public void removeTeam(Team team) {
        if(team == null)
            throw new ValidationException("Team can't be null");

        if(!hasStarted())
            throw new ConflictException("Team can't be removed while session not started");

        if(!teams.contains(team))
            throw new NotFoundException("Team not found with Id: " + team.getNumber());

        if(isCurrentMatchTeam(team))
            throw new ConflictException("Team can't be removed while it is participating in the current match");

        teams.remove(team);

        waitingQueue.remove(team);
    }

    public void removeEmptyTeams() {
        List<Team> emptyTeams = teams.stream()
                .filter(t -> !t.hasPlayers() &&
                        !isCurrentMatchTeam(t))
                .toList();

        for(Team team : emptyTeams) {
            removeTeam(team);
        }
    }

    public Team findPlayerTeam(UUID playerId) {
        if (playerId == null)
            throw new ValidationException("Player Id can't be null");

        return teams.stream()
                .filter(team -> team.containsPlayer(playerId))
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException("Player not found in session with Id: " + playerId)
                );
    }

    public Team getLastTeam() {
        return teams.isEmpty()
                ? null
                : teams.getLast();
    }

    public boolean hasStarted() {
        return currentMatch != null;
    }

    public boolean isCurrentMatchTeam(Team team) {
        if(!hasStarted())
            return false;

        return currentMatch.getTeamA().equals(team) || currentMatch.getTeamB().equals(team);
    }

    public void initializeSession() {
        if (hasStarted())
            throw new ConflictException("Session already initialized");

        if(teams.size() < INITIAL_TEAMS)
            throw new ConflictException("Not enough getTeams to start");

        this.waitingQueue = new WaitingQueue(teams);

        Team t1 = waitingQueue.poll();
        Team t2 = waitingQueue.poll();

        this.currentMatch = new Match(t1, t2);
    }

    public void markInitialTeamsAsCreated() {
        this.initialTeamsCreated = true;
    }

    public boolean canCreateInitialTeams() {
        return activePlayers.size() == INITIAL_PLAYERS
                && !isInitialTeamsCreated();
    }

    public void startNextMatch(Match match) {
        if(match == null)
            throw new ValidationException("Match can't be null");

        this.currentMatch = match;
    }

    // Verificar uso
    public void setTeams(List<Team> teams) {
        if(teams == null)
            throw new ValidationException("Teams can't be null");

        // Observar comportamento antes de finalizar Sprint
        this.teams = new ArrayList<>(teams);
    }

    public List<Team> getTeams() {
        return List.copyOf(teams);
    }

    public List<Player> getActivePlayers() {
        return List.copyOf(activePlayers);
    }
}
