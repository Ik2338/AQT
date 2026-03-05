package com.ecommerce.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Produit;

@Repository
public interface ProduitRepository extends JpaRepository<Produit, Long> {
    List<Produit> findByActifTrue();
    List<Produit> findByNomContainingIgnoreCaseAndActifTrue(String nom);
    List<Produit> findByCategorieIdAndActifTrue(Long categorieId);
    List<Produit> findByNomContainingIgnoreCaseAndCategorieIdAndActifTrue(String nom, Long categorieId);
}
