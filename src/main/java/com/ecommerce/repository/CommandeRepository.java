package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Commande;

// Accès base de données pour l'entité Commande
@Repository
public interface CommandeRepository extends JpaRepository<Commande, Long> {

    // Retourne les commandes d'un client triées par date décroissante (plus récente en premier)
    List<Commande> findByClientIdOrderByDateCommandeDesc(Long clientId);
}