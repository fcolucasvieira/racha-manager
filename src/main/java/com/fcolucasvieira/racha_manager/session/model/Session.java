package com.fcolucasvieira.racha_manager.session.model;

import com.fcolucasvieira.racha_manager.common.exception.ConflictException;
import com.fcolucasvieira.racha_manager.common.exception.NotFoundException;
import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import com.fcolucasvieira.racha_manager.player.model.Player;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.INITIAL_TEAMS;
import static com.fcolucasvieira.racha_manager.session.constant.RachaRules.TEAM_SIZE;

@Getter
public class Session {
    private final UUID id;
    private final List<Player> activePlayers;
    private List<Team> teams;
    // Objeto próprio? (Waiting Queue)
    private WaitingQueue waitingQueue;
    private List<Team> queue;
    private Match currentMatch;

    // Nome de atributo melhor? (trazer + clareza)
    private boolean shuffled;

    public Session() {
        this.id = UUID.randomUUID();
        this.activePlayers = new ArrayList<>();

        // (Teams) Iniciar apenas quando houver da qtde. mínima de jogadores ativos p/ iniciar sessão?
        this.teams = new ArrayList<>();
        this.waitingQueue = null;
        this.queue = null;
        this.shuffled = false;
    }

    // Player
    // -> Adicionar jogador a lista de jogadores ativos da sessão
    // -> Validar se jogador pode ser adicionado a lista de jogadores ativos da sessão
    // -> Remover jogador da lista de jogadores ativos da sessão
    // -> Buscar time no qual o jogador está na sessão
    public void addPlayer(Player player) {
        validatePlayerForAddition(player);
        activePlayers.add(player);
    }

    // Invariante repetida: um jogador não entra duas vezes na sessão
    private void validatePlayerForAddition(Player player) {
        // Retirar (player.getId == null) (Construtor SEMPRE inicializa este atributo)
        if(player == null || player.getId() == null) {
            throw new ValidationException("Player or player ID cannot be null");
        }

        boolean alreadyExists = activePlayers.stream()
                .anyMatch(p-> p.getId().equals(player.getId()));

        if (alreadyExists) {
            throw new ConflictException("Player already in session");
        }
    }

    public void removePlayer(UUID playerId) {
        if (playerId == null) {
            throw new ValidationException("Player ID cannot be null");
        }

        boolean removed = activePlayers
                .removeIf(p -> p.getId().equals(playerId));

        if(!removed){
            throw new NotFoundException("Player not found in session");
        }
    }

    public Team findPlayerTeam(UUID playerId) {
        if(playerId == null) {
            throw new ValidationException("Player ID cannot be null");
        }

        return teams.stream()
                .filter(team -> team.getPlayers().stream()
                        .anyMatch(player ->
                                player.getId().equals(playerId)))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Player is not in a team"));
    }

    // Team
    // -> Inserir nova lista de times à sessão (substituir antiga)
    // -> Remover time da lista de times da sessão

    // Nome de atributo melhor? (trazer + clareza)
    public void setTeams(List<Team> teams) {
        if(teams == null){
            throw new ValidationException("Teams cannot be null");
        }

        this.teams = teams;
    }

    public void removeTeam(Team team) {
        if(team == null) {
            throw new ValidationException("Team cannot be null");
        }

        // Validação duplicada? (Se time estiver na partida atual, então a sessão já iniciou)
        if(hasStarted() && isCurrentMatchTeam(team)) {
            throw new ConflictException("Cannot remove all players from current match team");
        }

        teams.remove(team);

        if(queue != null){
            queue.remove(team);
        }
    }

    // CurrentMatch
    // -> Verificar se sessão já iniciou
    // -> Verificar se time está na partida atual da sessão
    // -> Setar/Atualizar partida atual da sessão

    // Nome de método melhor? (trazer + clareza)
    public boolean hasStarted() {
        return currentMatch != null;
    }

    public boolean isCurrentMatchTeam(Team team) {
        if(!hasStarted()) {
            return false;
        }

        return currentMatch.getTeamA().equals(team) || currentMatch.getTeamB().equals(team);
    }

    public void setCurrentMatch(Match match) {
        if(match == null) {
            throw new ValidationException("Match cannot be null");
        }

        this.currentMatch = match;
    }

    // Waiting Queue deve ser objeto próprio!

    // Queue
    // -> Iniciar fila de espera da sessão
    // -> Carregar/Exibir fila de espera
    // -> Adicionar time a fila de espera
    // -> Remover primeiro time da fila de espera
    // -> Verificar se existe fila de espera
    // -> Verificar se há jogadores suficientes na lista de espera para realizar empate na sessão
    // -> Realizar contagem de jogadores na fila de espera da sessão

    public void startQueue() {
        if (hasStarted()) {
            throw new ConflictException("Queue already started");
        }

        if(teams.size() < INITIAL_TEAMS){
            throw new ConflictException("Not enough teams to start");
        }

        this.queue = new ArrayList<>(teams);

        this.waitingQueue = new WaitingQueue(queue);

        Team t1 = queue.removeFirst();
        Team t2 = queue.removeFirst();

        this.currentMatch = new Match(t1, t2);
    }

    public List<Team> getQueue() {
        return queue == null ? List.of() : List.copyOf(queue);
    }

    public void addTeamToQueue(Team team) {
        if(team == null) {
            throw new ValidationException("Team cannot be null");
        }

        if(this.queue == null) {
            throw new ConflictException("Queue not initialized");
        }

        if(this.queue.contains(team)) {
            throw new ConflictException("Team already in queue");
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

                return;
            }

            // seleta o último time da queue como novato
            Team lastRookie = rookieTeams.getLast();

            // seleta índice do último time novato na queue
            int rookieIndex = queue.indexOf(lastRookie);

            this.queue.add(rookieIndex + 1, team);

            return;
        }

        // caso contrário, adicioná-lo ao final da fila
        this.queue.add(team);
    }

    public Team removeFirstTeamFromQueue() {
        if(queue == null || queue.isEmpty()) {
            throw new ConflictException("Queue is empty");
        }

        return queue.removeFirst();
    }

    public boolean hasQueue() {
        return queue != null;
    }

    public boolean hasEnoughPlayersForDraw() {
        if(waitingQueue == null)
            return false;

        return waitingQueue.hasEnoughForDraw();
    }

    public int getWaitingPlayersCount() {
        if(waitingQueue == null)
            return 0;

        return waitingQueue.playersCount();
    }

    // Shuffled
    // -> Marcar sessão como embaralhada

    public void markAsShuffled() {
        this.shuffled = true;
    }
}
