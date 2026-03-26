package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Role;

// Accès base de données pour l'entité Role
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Recherche un rôle par son nom (ex: "ROLE_ADMIN", "ROLE_CLIENT")
    Optional<Role> findByNom(String nom);
}