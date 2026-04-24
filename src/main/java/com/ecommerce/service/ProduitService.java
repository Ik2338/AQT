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

    private final ProduitRepository repo;
    private final ApplicationContext applicationContext;

    public ProduitService(ProduitRepository repo, ApplicationContext applicationContext) {
        this.repo               = repo;
        this.applicationContext = applicationContext;
    }

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
        return repo.findAll(); // ✅ retourne actifs ET inactifs pour l'admin
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
     */
    public void supprimer(Long id) {
        Produit p = self().trouverParId(id);
        p.setActif(false);
        repo.save(p);
    }

    /**
     * ✅ Toggle actif/inactif : inverse l'état du produit.
     * Retourne le nouvel état (true = actif, false = inactif).
     */
    public boolean toggleActif(Long id) {
        Produit p = self().trouverParId(id);
        p.setActif(!p.isActif());
        repo.save(p);
        return p.isActif();
    }

    public void mettreAJourStock(Long id, int stock) {
        Produit p = self().trouverParId(id);
        p.setStock(stock);
        repo.save(p);
    }
}