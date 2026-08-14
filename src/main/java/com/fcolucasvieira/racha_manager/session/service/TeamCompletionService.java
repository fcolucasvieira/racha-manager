package com.fcolucasvieira.racha_manager.session.service;

import com.fcolucasvieira.racha_manager.session.model.Team;
import com.fcolucasvieira.racha_manager.session.model.WaitingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class TeamCompletionService {
    private static final Logger log = LoggerFactory.getLogger(TeamCompletionService.class);

    public void complete(Team target, WaitingQueue waitingQueue) {
        for (Team donor : waitingQueue.getTeams()) {
            while (target.isIncomplete() &&
                    donor.hasPlayers()) {
                var transferredPlayer = donor.removeFirstPlayer();

                target.addPlayer(transferredPlayer);

                log.info(
                        "[PLAYER_TRANSFERRED] donorTeam={} targetTeam={} playerId={}",
                        donor.getNumber(),
                        target.getNumber(),
                        transferredPlayer.getId()
                );
            }

            if (target.isFull())
                return;
        }
    }
}
