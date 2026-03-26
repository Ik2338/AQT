package com.ecommerce.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Produit;

// Accès base de données pour l'entité Produit
@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {

    // Tous les produits actifs
    List<Produit> findByActifTrue();

    // Recherche par nom (insensible à la casse) parmi les produits actifs
    List<Produit> findByNomContainingIgnoreCaseAndActifTrue(String nom);

    // Produits d'une catégorie donnée
    List<Produit> findByCategorieId(Long categorieId);

    // Produits actifs d'une catégorie donnée
    List<Produit> findByCategorieIdAndActifTrue(Long categorieId);

    // Recherche par nom et catégorie parmi les produits actifs
    List<Produit> findByNomContainingIgnoreCaseAndCategorieIdAndActifTrue(String nom, Long categorieId);
}