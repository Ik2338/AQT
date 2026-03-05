package com.ecommerce.service;
import java.util.List;

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

@Service @Transactional
public class CommandeService {
    private final CommandeRepository commandeRepo;
    private final ProduitRepository produitRepo;
    private final PanierService panierService;

    public CommandeService(CommandeRepository commandeRepo, ProduitRepository produitRepo, PanierService panierService) {
        this.commandeRepo=commandeRepo; this.produitRepo=produitRepo; this.panierService=panierService;
    }

    /** R1+R2+R5 : Valider le panier et creer une commande */
    public Commande validerPanier(Utilisateur client, Panier panier, String adresse) {
        if (panier.getLignes().isEmpty()) {
			throw new RuntimeException("Le panier est vide.");
		}

        Commande commande = new Commande(client);
        commande.setAdresseLivraison(adresse);

        for (LignePanier lp : panier.getLignes()) {
            Produit produit = lp.getProduit();
            if (produit.getStock() < lp.getQuantite()) {
				throw new StockInsuffisantException(produit.getNom(), produit.getStock(), lp.getQuantite());
			}
            // R5 : decrementer le stock
            produit.setStock(produit.getStock() - lp.getQuantite());
            produitRepo.save(produit);
            commande.getLignes().add(new LigneCommande(commande, produit, lp.getQuantite()));
        }
        commande.setEtat(EtatCommande.VALIDEE);
        commande.recalculerTotal();
        Commande saved = commandeRepo.save(commande);
        panierService.vider(client);
        return saved;
    }

    @Transactional(readOnly=true)
    public List<Commande> historiqueClient(Long clientId) {
        return commandeRepo.findByClientIdOrderByDateCommandeDesc(clientId);
    }

    @Transactional(readOnly=true)
    public List<Commande> toutesLesCommandes() { return commandeRepo.findAll(); }

    @Transactional(readOnly=true)
    public Commande trouverParId(Long id) {
        return commandeRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Commande", id));
    }

    public Commande changerEtat(Long id, EtatCommande etat) {
        Commande c = trouverParId(id); c.setEtat(etat); return commandeRepo.save(c);
    }
}
