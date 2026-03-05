package com.ecommerce.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.PanierRepository;
import com.ecommerce.repository.ProduitRepository;

@Service @Transactional
public class PanierService {
    private final PanierRepository panierRepo;
    private final ProduitRepository produitRepo;

    public PanierService(PanierRepository panierRepo, ProduitRepository produitRepo) {
        this.panierRepo = panierRepo; this.produitRepo = produitRepo;
    }

    public Panier obtenirOuCreer(Utilisateur utilisateur) {
        return panierRepo.findByUtilisateurId(utilisateur.getId())
            .orElseGet(() -> panierRepo.save(new Panier(utilisateur)));
    }

    public Panier ajouterProduit(Utilisateur utilisateur, Long produitId, int quantite) {
        Panier panier = obtenirOuCreer(utilisateur);
        Produit produit = produitRepo.findById(produitId)
            .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        LignePanier existante = panier.getLignes().stream()
            .filter(l -> l.getProduit().getId().equals(produitId))
            .findFirst().orElse(null);

        int qteFinale = (existante != null ? existante.getQuantite() : 0) + quantite;
        if (produit.getStock() < qteFinale) {
			throw new StockInsuffisantException(produit.getNom(), produit.getStock(), qteFinale);
		}

        if (existante != null) {
            existante.setQuantite(qteFinale);
        } else {
            panier.getLignes().add(new LignePanier(panier, produit, quantite));
        }
        return panierRepo.save(panier);
    }

    public Panier modifierQuantite(Utilisateur utilisateur, Long ligneId, int quantite) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().stream()
            .filter(l -> l.getId().equals(ligneId))
            .findFirst().ifPresent(l -> {
                if (quantite <= 0) {
					panier.getLignes().remove(l);
				} else {
					l.setQuantite(quantite);
				}
            });
        return panierRepo.save(panier);
    }

    public Panier supprimerLigne(Utilisateur utilisateur, Long ligneId) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().removeIf(l -> l.getId().equals(ligneId));
        return panierRepo.save(panier);
    }

    public void vider(Utilisateur utilisateur) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().clear();
        panierRepo.save(panier);
    }
}
