package com.ecommerce.service;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.ProduitRepository;

@Service
@Transactional
public class ProduitService {

    private final ProduitRepository  repo;

    /**
     * ApplicationContext utilisé pour obtenir le proxy Spring de ce service
     * et éviter la self-invocation sur les méthodes @Transactional(readOnly=true).
     */
    private final ApplicationContext applicationContext;

    public ProduitService(ProduitRepository repo, ApplicationContext applicationContext) {
        this.repo               = repo;
        this.applicationContext = applicationContext;
    }

    /** Retourne le proxy Spring de ce service (évite la self-invocation). */
    private ProduitService self() {
        return applicationContext.getBean(ProduitService.class);
    }

    // ─── Recherche ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Produit> rechercher(String nom, Long categorieId) {
        if (nom != null && !nom.isBlank() && categorieId != null) {
            return repo.findByNomContainingIgnoreCaseAndCategorieIdAndActifTrue(nom, categorieId);
        }
        if (nom != null && !nom.isBlank()) {
            return repo.findByNomContainingIgnoreCaseAndActifTrue(nom);
        }
        if (categorieId != null) {
            return repo.findByCategorieIdAndActifTrue(categorieId);
        }
        return repo.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public List<Produit> listerTous() {
        return repo.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public Produit trouverParId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit", id));
    }

    // ─── Écriture ────────────────────────────────────────────────────────────

    public Produit creer(Produit p) {
        p.setActif(true);
        return repo.save(p);
    }

    /**
     * Met à jour tous les champs d'un produit existant.
     * Utilise self() pour appeler trouverParId via le proxy Spring
     * et garantir l'interception @Transactional(readOnly=true).
     */
    public Produit modifier(Long id, Produit data) {
        Produit p = self().trouverParId(id);
        p.setNom(data.getNom());
        p.setDescription(data.getDescription());
        p.setPrix(data.getPrix());
        p.setStock(data.getStock());
        p.setCategorie(data.getCategorie());
        p.setImageUrl(data.getImageUrl());
        return repo.save(p);
    }

    /**
     * Suppression douce (soft delete) : désactive le produit.
     * Utilise self() pour éviter la self-invocation.
     */
    public void supprimer(Long id) {
        Produit p = self().trouverParId(id);
        p.setActif(false);
        repo.save(p);
    }

    /**
     * Met à jour uniquement le stock d'un produit.
     * Utilise self() pour éviter la self-invocation.
     */
    public void mettreAJourStock(Long id, int stock) {
        Produit p = self().trouverParId(id);
        p.setStock(stock);
        repo.save(p);
    }
}