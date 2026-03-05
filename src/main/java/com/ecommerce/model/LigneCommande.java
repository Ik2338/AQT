package com.ecommerce.model;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity @Table(name = "ligne_commande")
public class LigneCommande {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "commande_id", nullable = false) private Commande commande;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "produit_id", nullable = false) private Produit produit;
    @Column(nullable = false) private Integer quantite;
    @Column(name = "prix_unitaire", nullable = false) private Double prixUnitaire;

    public LigneCommande() {}
    public LigneCommande(Commande commande, Produit produit, Integer quantite) {
        this.commande=commande; this.produit=produit; this.quantite=quantite; this.prixUnitaire=produit.getPrix();
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Commande getCommande() { return commande; } public void setCommande(Commande c) { this.commande = c; }
    public Produit getProduit() { return produit; } public void setProduit(Produit p) { this.produit = p; }
    public Integer getQuantite() { return quantite; } public void setQuantite(Integer q) { this.quantite = q; }
    public Double getPrixUnitaire() { return prixUnitaire; } public void setPrixUnitaire(Double p) { this.prixUnitaire = p; }
    public Double getSousTotal() { return prixUnitaire * quantite; }
}
