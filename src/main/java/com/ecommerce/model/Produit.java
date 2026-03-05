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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity @Table(name = "produit")
public class Produit {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @NotBlank @Column(nullable = false) private String nom;
    @Column(length = 1000) private String description;
    @NotNull @DecimalMin("0.01") @Column(nullable = false) private Double prix;
    @NotNull @Min(0) @Column(nullable = false) private Integer stock;
    @Column(nullable = false) private boolean actif = true;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "categorie_id") private Categorie categorie;
    public Produit() {}
    public Produit(String nom, String description, Double prix, Integer stock, Categorie categorie) {
        this.nom=nom; this.description=description; this.prix=prix; this.stock=stock; this.categorie=categorie; this.actif=true;
    }
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; } public void setNom(String nom) { this.nom = nom; }
    public String getDescription() { return description; } public void setDescription(String d) { this.description = d; }
    public Double getPrix() { return prix; } public void setPrix(Double prix) { this.prix = prix; }
    public Integer getStock() { return stock; } public void setStock(Integer stock) { this.stock = stock; }
    public boolean isActif() { return actif; } public void setActif(boolean actif) { this.actif = actif; }
    public Categorie getCategorie() { return categorie; } public void setCategorie(Categorie c) { this.categorie = c; }
}
