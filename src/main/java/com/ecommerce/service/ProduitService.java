package com.ecommerce.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.ProduitRepository;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository repo;

    // Injection du repository via constructeur
    public ProduitService(ProduitRepository repo) {
        this.repo = repo;
    }

    // Recherche filtrée par nom et/ou catégorie, parmi les produits actifs
    @Transactional(readOnly = true)
    public List<Produit> rechercher(String nom, Long categorieId) {
        if (nom != null && !nom.isBlank() && categorieId != null)
            return repo.findByNomContainingIgnoreCaseAndCategorieIdAndActifTrue(nom, categorieId);
        if (nom != null && !nom.isBlank())
            return repo.findByNomContainingIgnoreCaseAndActifTrue(nom);
        if (categorieId != null)
            return repo.findByCategorieIdAndActifTrue(categorieId);
        // Aucun filtre : retourne tous les produits actifs
        return repo.findByActifTrue();
    }

    // Retourne tous les produits actifs
    @Transactional(readOnly = true)
    public List<Produit> listerTous() {
        return repo.findByActifTrue();
    }

    // Recherche un produit par ID, lève une exception si introuvable
    @Transactional(readOnly = true)
    public Produit trouverParId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));
    }

    // Crée un nouveau produit et le marque comme actif
    public Produit creer(Produit p) {
        p.setActif(true);
        return repo.save(p);
    }

    // Met à jour les informations d'un produit existant
    public Produit modifier(Long id, Produit data) {
        Produit p = trouverParId(id);
        p.setNom(data.getNom());
        p.setDescription(data.getDescription());
        p.setPrix(data.getPrix());
        p.setStock(data.getStock());
        p.setCategorie(data.getCategorie());
        p.setImageUrl(data.getImageUrl());
        return repo.save(p);
    }

    // Suppression logique : désactive le produit sans le supprimer de la base
    public void supprimer(Long id) {
        Produit p = trouverParId(id);
        p.setActif(false);
        repo.save(p);
    }

    // Met à jour uniquement le stock d'un produit
    public void mettreAJourStock(Long id, int stock) {
        Produit p = trouverParId(id);
        p.setStock(stock);
        repo.save(p);
    }
}