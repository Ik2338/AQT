package com.ecommerce.service;

import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.Commande;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.model.LigneCommande;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.CommandeRepository;
import com.ecommerce.repository.ProduitRepository;

@Service
@Transactional
public class CommandeService {

    private final CommandeRepository commandeRepo;
    private final ProduitRepository  produitRepo;
    private final PanierService      panierService;

    /**
     * ApplicationContext utilisé pour obtenir le proxy Spring de ce service
     * afin d'éviter la self-invocation et de garantir que les méthodes
     * @Transactional(readOnly=true) sont bien interceptées.
     */
    private final ApplicationContext applicationContext;

    public CommandeService(CommandeRepository commandeRepo,
                           ProduitRepository produitRepo,
                           PanierService panierService,
                           ApplicationContext applicationContext) {
        this.commandeRepo       = commandeRepo;
        this.produitRepo        = produitRepo;
        this.panierService      = panierService;
        this.applicationContext = applicationContext;
    }

    /** Retourne le proxy Spring de ce service (évite la self-invocation). */
    private CommandeService self() {
        return applicationContext.getBean(CommandeService.class);
    }

    // ─── Valider le panier et créer une commande ─────────────────────────────

    public Commande validerPanier(Utilisateur client, Panier panier, String adresse) {
        if (panier.getLignes().isEmpty()) {
            throw new IllegalStateException("Le panier est vide.");
        }

        Commande commande = new Commande(client);
        commande.setAdresseLivraison(adresse);

        for (LignePanier lp : panier.getLignes()) {
            Produit produit = lp.getProduit();
            if (produit.getStock() < lp.getQuantite()) {
                throw new StockInsuffisantException(
                        produit.getNom(), produit.getStock(), lp.getQuantite());
            }
            produit.setStock(produit.getStock() - lp.getQuantite());
            produitRepo.save(produit);
            commande.getLignes().add(
                    new LigneCommande(commande, produit, lp.getQuantite()));
        }

        commande.setEtat(EtatCommande.VALIDEE);
        commande.recalculerTotal();
        Commande saved = commandeRepo.save(commande);
        panierService.vider(client);
        return saved;
    }

    // ─── Lecture ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Commande> historiqueClient(Long clientId) {
        return commandeRepo.findByClientIdOrderByDateCommandeDesc(clientId);
    }

    @Transactional(readOnly = true)
    public List<Commande> toutesLesCommandes() {
        return commandeRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Commande trouverParId(Long id) {
        return commandeRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", id));
    }

    // ─── Changement d'état ───────────────────────────────────────────────────

    /**
     * Change l'état d'une commande.
     * Utilise self() pour appeler trouverParId via le proxy Spring
     * et garantir l'interception @Transactional(readOnly=true).
     */
    public Commande changerEtat(Long id, EtatCommande etat) {
        Commande c = self().trouverParId(id);
        c.setEtat(etat);
        return commandeRepo.save(c);
    }
}