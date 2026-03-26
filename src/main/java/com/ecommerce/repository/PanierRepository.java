package com.ecommerce.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Panier;

// Accès base de données pour l'entité Panier
@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {

    // Récupère le panier d'un utilisateur par son ID
    Optional<Panier> findByUtilisateurId(Long utilisateurId);
}