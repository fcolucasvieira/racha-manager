package com.fcolucasvieira.racha_manager.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Table(name = "players")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;
}
