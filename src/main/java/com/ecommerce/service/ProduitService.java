package com.ecommerce.service;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.ProduitRepository;

@Service @Transactional
public class ProduitService {
    private final ProduitRepository repo;
    public ProduitService(ProduitRepository repo) { this.repo = repo; }

    @Transactional(readOnly=true)
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

    @Transactional(readOnly=true)
    public List<Produit> listerTous() { return repo.findByActifTrue(); }

    @Transactional(readOnly=true)
    public Produit trouverParId(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Produit", id));
    }

    public Produit creer(Produit p) { p.setActif(true); return repo.save(p); }

    public Produit modifier(Long id, Produit data) {
        Produit p = trouverParId(id);
        p.setNom(data.getNom()); p.setDescription(data.getDescription());
        p.setPrix(data.getPrix()); p.setStock(data.getStock()); p.setCategorie(data.getCategorie());
        return repo.save(p);
    }

    public void supprimer(Long id) {
        Produit p = trouverParId(id); p.setActif(false); repo.save(p);
    }

    public void mettreAJourStock(Long id, int stock) {
        Produit p = trouverParId(id); p.setStock(stock); repo.save(p);
    }
}
