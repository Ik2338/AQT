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

@Entity @Table(name = "ligne_panier")
public class LignePanier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "panier_id", nullable = false) private Panier panier;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "produit_id", nullable = false) private Produit produit;
    @Column(nullable = false) private Integer quantite;

    public LignePanier() {}
    public LignePanier(Panier panier, Produit produit, Integer quantite) {
        this.panier=panier; this.produit=produit; this.quantite=quantite;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Panier getPanier() { return panier; } public void setPanier(Panier p) { this.panier = p; }
    public Produit getProduit() { return produit; } public void setProduit(Produit p) { this.produit = p; }
    public Integer getQuantite() { return quantite; } public void setQuantite(Integer q) { this.quantite = q; }
    public Double getSousTotal() { return produit.getPrix() * quantite; }
}
