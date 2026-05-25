package com.fcolucasvieira.racha_manager.domain.model;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class Session {

    private final UUID id;
    private final List<PlayerEntity> activePlayers;
    private List<Team> teams;
    private List<Team> queue;
    private Match currentMatch;
    private boolean shuffled;

    private static final Logger log =  LoggerFactory.getLogger(Session.class);

    public Session() {
        this.id = UUID.randomUUID();
        this.activePlayers = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.queue = null;
        this.shuffled = false;
    }

    public void addPlayer(PlayerEntity player) {
        validatePlayerForAddition(player);
        activePlayers.add(player);
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null) {
            throw new IllegalArgumentException("Player ID cannot be null");
        }

        boolean removed = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!removed){
            throw new IllegalArgumentException("Player not found in session");
        }
    }

    private void validatePlayerForAddition(PlayerEntity player) {
        if(player == null || player.getId() == null) {
            throw new IllegalArgumentException("Player cannot be null");
        }

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new IllegalArgumentException("Player already in session");
        }
    }

    public void updateTeams(List<Team> teams) {
        if(teams == null){
            throw new IllegalArgumentException("Teams cannot be null");
        }

        this.teams = teams;
    }

    public void removeTeam(Team team) {
        if(team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }

        if(teams != null) {
            teams.remove(team);
        }

        if(queue != null){
            queue.remove(team);
        }
    }

    public boolean hasStarted() {
        return currentMatch != null;
    }

    public void updateCurrentMatch(Match match) {
        if(match == null) {
            throw new IllegalArgumentException("Match cannot be null");
        }

        this.currentMatch = match;
    }

    // regras de negócio (fila) em session
    public void startQueue() {
        if (currentMatch != null) {
            throw new IllegalStateException("Queue already started");
        }

        if(teams == null || teams.size() < 2){
            throw new IllegalStateException("Not enough teams to start");
        }

        this.queue = new ArrayList<>(teams);

        // armazena os dois primeiros times da fila (jogo atual)
        Team t1 = queue.removeFirst();
        Team t2 = queue.removeFirst();

        this.currentMatch = new Match(t1, t2);
    }

    public List<Team> getQueue() {
        return queue == null ? List.of() : List.copyOf(queue);
    }

    public void addTeamToQueue(Team team) {
        if(team == null) {
            throw new IllegalArgumentException("Team cannot be null");
        }

        if(this.queue == null) {
            throw new IllegalStateException("Queue not initialized");
        }

        if(this.queue.contains(team)) {
            throw new IllegalStateException("Team already in queue");
        }

        // se o time ainda não jogou, adicioná-lo atrás do último novato da fila
        if(!team.isPlayed()) {

            // gera uma lista com times novatos através da queue
            List<Team> rookieTeams = queue.stream()
                    .filter(t -> !t.isPlayed())
                    .toList();

            // se a lista estiver vazia, não há times novatos. logo, o time novato é adicionado ao início da queue
            if(rookieTeams.isEmpty()) {
                this.queue.addFirst(team);

                /*
                log.info(
                        "[QUEUE_PRIORITY] team={} rookie=true insertedPosition={} queue={}",
                        team.getNumber(),
                        0,
                        queue.stream().map(Team::getNumber).toList()
                );
                 */

                return;
            }

            // seleta o último time da queue como novato
            Team lastRookie = rookieTeams.getLast();

            // seleta índice do último time novato na queue
            int rookieIndex = queue.indexOf(lastRookie);

            this.queue.add(rookieIndex + 1, team);

            /*
            log.info(
                    "[QUEUE_PRIORITY] team={} rookie=true insertedPosition={} queue={}",
                    team.getNumber(),
                    rookieIndex + 1,
                    queue.stream().map(Team::getNumber).toList()
            );
             */

            return;
        }

        // caso contrário, adicioná-lo ao final da fila
        this.queue.add(team);

        /*
        log.info(
                "[QUEUE_APPEND] team={} rookie=false insertedPosition={} queue={}",
                team.getNumber(),
                queue.size() - 1,
                queue.stream().map(Team::getNumber).toList()
        );
         */
    }

    public Team removeFirstTeamFromQueue() {
        if(queue == null || queue.isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }

        return queue.removeFirst();
    }

    public boolean hasQueue() {
        return queue != null;
    }

    public void clearQueue() {
        if (this.queue != null) {
            this.queue.clear();
        }
        this.currentMatch = null;
    }

    public boolean hasAtLeastTeamsInQueue(int amount) {
        return queue != null && queue.size() >= amount;
    }

    public boolean canStartQueue() {
        return !hasStarted() && teams != null && teams.size() >= 2;
    }

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
