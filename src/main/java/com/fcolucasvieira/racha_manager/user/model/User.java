package com.fcolucasvieira.racha_manager.user.model;

import com.fcolucasvieira.racha_manager.common.exception.ValidationException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    // PK (Application Spring Boot)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // Authentication provider (Local, Google and Github) -> Only Google actually
    private Provider provider;

    @Column(name = "provider_id", unique = true, nullable = false)
    // (Sub) Authentication ID provider
    private String providerId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    public User(Provider provider, String providerId, String email, String name) {
        if(provider == null)
            throw new ValidationException("Provider can't be null");

        this.provider = provider;

        if(providerId == null || providerId.isBlank())
            throw new ValidationException("Provider id can't be null or blank");

        this.providerId = providerId;

        if(email == null || email.isBlank())
            throw new ValidationException("Email can't not be null or blank");

        this.email = email;

        if(name == null || name.isBlank())
            throw new ValidationException("Name can't not be null or blank");

        this.name = name;
    }
}
