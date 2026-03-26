package com.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecommerce.model.Categorie;

// Accès base de données pour l'entité Categorie
// Hérite des opérations CRUD de base (findAll, findById, save, delete...)
@Repository
public interface CategorieRepository extends JpaRepository<Categorie, Long> {}