package com.ecommerce.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

// Panier de l'utilisateur, persisté en base de données
@Entity
@Table(name = "panier")
public class Panier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Chaque utilisateur possède un seul panier
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", unique = true)
    private Utilisateur utilisateur;

    // Lignes du panier, supprimées automatiquement si retirées
    @OneToMany(mappedBy = "panier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LignePanier> lignes = new ArrayList<>();

    public Panier() {}

    public Panier(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }

    // Calcule le montant total du panier
    public double getTotal() {
        // CORRECTION : Éviter NullPointerException
        if (lignes == null || lignes.isEmpty()) {
            return 0.0;
        }
        return lignes.stream()
                .mapToDouble(ligne -> {
                    if (ligne.getProduit() == null) {
                        return 0.0;
                    }
                    return ligne.getProduit().getPrix() * ligne.getQuantite();
                })
                .sum();
    }
    

    // Retourne le nombre total d'articles dans le panier
    public int getNombreArticles() {
        return lignes.stream()
                .mapToInt(LignePanier::getQuantite)
                .sum();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getUtilisateur() { return utilisateur; }
    public void setUtilisateur(Utilisateur utilisateur) { this.utilisateur = utilisateur; }

    public List<LignePanier> getLignes() { return lignes; }
    public void setLignes(List<LignePanier> lignes) { this.lignes = lignes; }
}