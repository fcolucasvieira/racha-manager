package com.fcolucasvieira.racha_manager.user.repository;

import com.fcolucasvieira.racha_manager.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
