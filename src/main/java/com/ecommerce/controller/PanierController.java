package com.ecommerce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.model.Commande;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.PanierService;
import com.ecommerce.service.UtilisateurService;

@Controller
@RequestMapping("/panier")
public class PanierController {

    private final PanierService panierService;
    private final CommandeService commandeService;
    private final UtilisateurService utilisateurService;

    // Injection des services via constructeur
    public PanierController(PanierService p, CommandeService c, UtilisateurService u) {
        this.panierService = p;
        this.commandeService = c;
        this.utilisateurService = u;
    }

    // Méthode utilitaire : récupère l'utilisateur connecté depuis l'authentification
    private Utilisateur getUser(Authentication auth) {
        return utilisateurService.trouverParEmail(auth.getName());
    }

    // Affiche le panier de l'utilisateur connecté
    @GetMapping
    public String voirPanier(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        model.addAttribute("panier", panierService.obtenirOuCreer(u));
        return "panier/panier";
    }

    // Ajoute un produit au panier (quantité par défaut : 1)
    @PostMapping("/ajouter")
    public String ajouter(@RequestParam Long produitId,
                          @RequestParam(defaultValue = "1") int quantite,
                          Authentication auth, RedirectAttributes ra) {
        try {
            panierService.ajouterProduit(getUser(auth), produitId, quantite);
            ra.addFlashAttribute("success", "Produit ajouté au panier !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panier";
    }

    // Modifie la quantité d'une ligne du panier
    @PostMapping("/modifier/{ligneId}")
    public String modifier(@PathVariable Long ligneId,
                           @RequestParam int quantite,
                           Authentication auth, RedirectAttributes ra) {
        panierService.modifierQuantite(getUser(auth), ligneId, quantite);
        return "redirect:/panier";
    }

    // Supprime une ligne du panier
    @PostMapping("/supprimer/{ligneId}")
    public String supprimer(@PathVariable Long ligneId, Authentication auth) {
        panierService.supprimerLigne(getUser(auth), ligneId);
        return "redirect:/panier";
    }

    // Affiche la page de confirmation avant validation de la commande
    @GetMapping("/checkout")
    public String checkout(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        Panier panier = panierService.obtenirOuCreer(u);
        // Redirige vers le panier si celui-ci est vide
        if (panier.getLignes().isEmpty()) {
            return "redirect:/panier";
        }
        model.addAttribute("panier", panier);
        model.addAttribute("utilisateur", u);
        return "panier/checkout";
    }

    // Valide le panier et crée la commande
    @PostMapping("/valider")
    public String valider(@RequestParam(required = false) String adresse,
                          Authentication auth, RedirectAttributes ra) {
        try {
            Utilisateur u = getUser(auth);
            Panier panier = panierService.obtenirOuCreer(u);
            Commande commande = commandeService.validerPanier(u, panier, adresse);
            ra.addFlashAttribute("success", "Commande #" + commande.getId() + " confirmée !");
            return "redirect:/commande/" + commande.getId();
        } catch (Exception e) {
            // En cas d'erreur (stock insuffisant, etc.), retourne au checkout
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/panier/checkout";
        }
    }
}