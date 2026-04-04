package com.ecommerce.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "lignes_panier")
public class LignePanier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "panier_id")
    @JsonIgnore
    private Panier panier;

    @ManyToOne
    @JoinColumn(name = "produit_id")
    private Produit produit;

    private int quantite;

    // Constructeurs
    public LignePanier() {}

    public LignePanier(Panier panier, Produit produit, int quantite) {
        this.panier = panier;
        this.produit = produit;
        this.quantite = quantite;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Panier getPanier() { return panier; }
    public void setPanier(Panier panier) { this.panier = panier; }

    public Produit getProduit() { return produit; }
    public void setProduit(Produit produit) { this.produit = produit; }

    public int getQuantite() { return quantite; }
    public void setQuantite(int quantite) { this.quantite = quantite; }

    /**
     * Calcule le sous-total pour cette ligne.
     * CORRECTION : Vérifie que produit n'est pas null
     */
    public double getSousTotal() {
        // ← CORRECTION CRITIQUE
        if (produit == null) {
            return 0.0;
        }
        return produit.getPrix() * quantite;
    }
}