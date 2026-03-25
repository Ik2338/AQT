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

@Service
@Transactional
public class PanierService {

    private final PanierRepository panierRepo;
    private final ProduitRepository produitRepo;

    // Injection des repositories via constructeur
    public PanierService(PanierRepository panierRepo, ProduitRepository produitRepo) {
        this.panierRepo = panierRepo;
        this.produitRepo = produitRepo;
    }

    // Retourne le panier existant ou en crée un nouveau pour l'utilisateur
    public Panier obtenirOuCreer(Utilisateur utilisateur) {
        return panierRepo.findByUtilisateurId(utilisateur.getId())
                .orElseGet(() -> panierRepo.save(new Panier(utilisateur)));
    }

    // Ajoute un produit au panier, ou incrémente la quantité si déjà présent
    public Panier ajouterProduit(Utilisateur utilisateur, Long produitId, int quantite) {
        Panier panier = obtenirOuCreer(utilisateur);
        Produit produit = produitRepo.findById(produitId)
                .orElseThrow(() -> new RuntimeException("Produit introuvable"));

        // Vérifie si le produit est déjà dans le panier
        LignePanier existante = panier.getLignes().stream()
                .filter(l -> l.getProduit().getId().equals(produitId))
                .findFirst().orElse(null);

        // Calcule la quantité finale (existante + nouvelle)
        int qteFinale = (existante != null ? existante.getQuantite() : 0) + quantite;

        // Vérifie la disponibilité du stock
        if (produit.getStock() < qteFinale) {
            throw new StockInsuffisantException(produit.getNom(), produit.getStock(), qteFinale);
        }

        if (existante != null) {
            existante.setQuantite(qteFinale); // Met à jour la quantité existante
        } else {
            panier.getLignes().add(new LignePanier(panier, produit, quantite)); // Nouvelle ligne
        }
        return panierRepo.save(panier);
    }

    // Modifie la quantité d'une ligne, ou la supprime si quantité <= 0
    public Panier modifierQuantite(Utilisateur utilisateur, Long ligneId, int quantite) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst().ifPresent(l -> {
                    if (quantite <= 0) {
                        panier.getLignes().remove(l); // Supprime la ligne si quantité invalide
                    } else {
                        l.setQuantite(quantite);
                    }
                });
        return panierRepo.save(panier);
    }

    // Supprime une ligne spécifique du panier
    public Panier supprimerLigne(Utilisateur utilisateur, Long ligneId) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().removeIf(l -> l.getId().equals(ligneId));
        return panierRepo.save(panier);
    }

    // Vide complètement le panier (appelé après validation d'une commande)
    public void vider(Utilisateur utilisateur) {
        Panier panier = obtenirOuCreer(utilisateur);
        panier.getLignes().clear();
        panierRepo.save(panier);
    }
}