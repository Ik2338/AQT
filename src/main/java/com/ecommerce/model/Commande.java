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

@Entity @Table(name = "commande")
public class Commande {
    public enum EtatCommande { EN_COURS, VALIDEE, ANNULEE }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Utilisateur client;
    @OneToMany(mappedBy = "commande", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCommande> lignes = new ArrayList<>();
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EtatCommande etat = EtatCommande.EN_COURS;
    @Column(name = "date_commande", nullable = false)
    private LocalDateTime dateCommande = LocalDateTime.now();
    @Column(name = "montant_total") private Double montantTotal = 0.0;
    @Column(length = 300) private String adresseLivraison;

    public Commande() {}
    public Commande(Utilisateur client) { this.client=client; this.etat=EtatCommande.EN_COURS; this.dateCommande=LocalDateTime.now(); }

    public boolean estModifiable() { return this.etat == EtatCommande.EN_COURS; }
    public void recalculerTotal() { this.montantTotal = lignes.stream().mapToDouble(l -> l.getPrixUnitaire() * l.getQuantite()).sum(); }

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Utilisateur getClient() { return client; } public void setClient(Utilisateur c) { this.client = c; }
    public List<LigneCommande> getLignes() { return lignes; } public void setLignes(List<LigneCommande> l) { this.lignes = l; }
    public EtatCommande getEtat() { return etat; } public void setEtat(EtatCommande e) { this.etat = e; }
    public LocalDateTime getDateCommande() { return dateCommande; } public void setDateCommande(LocalDateTime d) { this.dateCommande = d; }
    public Double getMontantTotal() { return montantTotal; } public void setMontantTotal(Double m) { this.montantTotal = m; }
    public String getAdresseLivraison() { return adresseLivraison; } public void setAdresseLivraison(String a) { this.adresseLivraison = a; }
}
