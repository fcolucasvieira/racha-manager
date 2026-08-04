package com.fcolucasvieira.racha_manager.session.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class WaitingQueue {
    private final List<Team> teams;

    public WaitingQueue() {
        this.teams = new ArrayList<>();
    }
}
