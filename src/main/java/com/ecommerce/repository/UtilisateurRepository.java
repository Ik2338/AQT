package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Utilisateur;

// Accès base de données pour l'entité Utilisateur
@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    // Recherche un utilisateur par email (utilisé pour l'authentification)
    Optional<Utilisateur> findByEmail(String email);

    // Vérifie si un email est déjà utilisé (utilisé lors de l'inscription)
    boolean existsByEmail(String email);
}