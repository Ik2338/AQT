package com.ecommerce.service;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categorie;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.CategorieRepository;
import com.ecommerce.repository.ProduitRepository;

@Service
@Transactional
public class CategorieService {

    private final CategorieRepository categorieRepo;
    private final ProduitRepository produitRepo;

    // Injection des repositories via constructeur
    public CategorieService(CategorieRepository categorieRepo, ProduitRepository produitRepo) {
        this.categorieRepo = categorieRepo;
        this.produitRepo = produitRepo;
    }

    // Retourne toutes les catégories (lecture seule)
    @Transactional(readOnly = true)
    public List<Categorie> listerToutes() {
        return categorieRepo.findAll();
    }

    // Recherche une catégorie par ID, lève une exception si introuvable
    @Transactional(readOnly = true)
    public Categorie trouverParId(Long id) {
        return categorieRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", id));
    }

    // Crée une nouvelle catégorie après validation du nom
    public Categorie creer(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire.");
        }
        Categorie cat = new Categorie();
        cat.setNom(nom.trim());
        return categorieRepo.save(cat);
    }

    // Supprime une catégorie et détache ses produits associés
    public void supprimer(Long id) {
        if (!categorieRepo.existsById(id)) {
            throw new ResourceNotFoundException("Categorie", id);
        }
        // Retire la catégorie de tous les produits liés avant suppression
        List<Produit> produits = produitRepo.findByCategorieId(id);
        for (Produit p : produits) {
            p.setCategorie(null);
            produitRepo.save(p);
        }
        categorieRepo.deleteById(id);
    }
}