package com.ecommerce.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Panier;
@Repository
public interface PanierRepository extends JpaRepository<Panier, Long> {
    Optional<Panier> findByUtilisateurId(Long utilisateurId);
}
