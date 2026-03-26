package com.ecommerce.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "commande")
public class Commande {

    // États possibles d'une commande
    public enum EtatCommande {
        EN_COURS,
        VALIDEE,
        ANNULEE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Client associé à la commande, chargé en lazy pour optimiser les requêtes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;

    // Lignes de la commande, supprimées automatiquement si retirées
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatCommande etat = EtatCommande.EN_COURS; // État initial

    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande = LocalDateTime.now();

    @Column(name = "montant_total")
    private Double montantTotal = 0.0;

    @Column(length = 300)
    private String adresseLivraison;

    public Commande() {}

    // Initialise une commande pour un client avec l'état EN_COURS
    public Commande(Utilisateur client) {
        this.client = client;
        this.etat = EtatCommande.EN_COURS;
        this.dateCommande = LocalDateTime.now();
    }

    // Vérifie si la commande peut encore être modifiée
    public boolean estModifiable() {
        return this.etat == EtatCommande.EN_COURS;
    }

    // Recalcule le montant total à partir des lignes de commande
    public void recalculerTotal() {
        this.montantTotal = lignes.stream()
                .mapToDouble(l -> l.getPrixUnitaire() * l.getQuantite())
                .sum();
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Utilisateur getClient() { return client; }
    public void setClient(Utilisateur client) { this.client = client; }

    public List<LigneCommande> getLignes() { return lignes; }
    public void setLignes(List<LigneCommande> lignes) { this.lignes = lignes; }

    public EtatCommande getEtat() { return etat; }
    public void setEtat(EtatCommande etat) { this.etat = etat; }

    public LocalDateTime getDateCommande() { return dateCommande; }
    public void setDateCommande(LocalDateTime dateCommande) { this.dateCommande = dateCommande; }

    public Double getMontantTotal() { return montantTotal; }
    public void setMontantTotal(Double montantTotal) { this.montantTotal = montantTotal; }

    public String getAdresseLivraison() { return adresseLivraison; }
    public void setAdresseLivraison(String adresseLivraison) { this.adresseLivraison = adresseLivraison; }
}