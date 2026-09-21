package com.fcolucasvieira.racha_manager.user.repository;

import com.fcolucasvieira.racha_manager.user.model.Provider;
import com.fcolucasvieira.racha_manager.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByProviderAndProviderId(Provider provider, String providerId);
}
