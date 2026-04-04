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

    public PanierController(PanierService p, CommandeService c, UtilisateurService u) {
        this.panierService = p;
        this.commandeService = c;
        this.utilisateurService = u;
    }

    // MÉTHODE MODIFIÉE : utilise Optional et gère le cas null
    private Utilisateur getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return utilisateurService.trouverParEmailOptional(auth.getName()).orElse(null);
    }

    @GetMapping
    public String voirPanier(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        if (u == null) {
            return "redirect:/login";
        }
        model.addAttribute("panier", panierService.obtenirOuCreer(u));
        return "panier/panier";
    }

    @PostMapping("/ajouter")
    public String ajouter(@RequestParam Long produitId,
                          @RequestParam(defaultValue = "1") int quantite,
                          Authentication auth, RedirectAttributes ra) {
        Utilisateur u = getUser(auth);
        if (u == null) {
            ra.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/login";
        }
        try {
            panierService.ajouterProduit(u, produitId, quantite);
            ra.addFlashAttribute("success", "Produit ajouté au panier !");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/panier";
    }

    @PostMapping("/modifier/{ligneId}")
    public String modifier(@PathVariable Long ligneId,
                           @RequestParam int quantite,
                           Authentication auth, RedirectAttributes ra) {
        Utilisateur u = getUser(auth);
        if (u == null) {
			return "redirect:/login";
		}
        panierService.modifierQuantite(u, ligneId, quantite);
        return "redirect:/panier";
    }

    @PostMapping("/supprimer/{ligneId}")
    public String supprimer(@PathVariable Long ligneId, Authentication auth) {
        Utilisateur u = getUser(auth);
        if (u == null) {
			return "redirect:/login";
		}
        panierService.supprimerLigne(u, ligneId);
        return "redirect:/panier";
    }

    @GetMapping("/checkout")
    public String checkout(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        if (u == null) {
			return "redirect:/login";
		}

        Panier panier = panierService.obtenirOuCreer(u);
        if (panier.getLignes().isEmpty()) {
            return "redirect:/panier";
        }
        model.addAttribute("panier", panier);
        model.addAttribute("utilisateur", u);
        return "panier/checkout";
    }

    @PostMapping("/valider")
    public String valider(@RequestParam(required = false) String adresse,
                          Authentication auth, RedirectAttributes ra) {
        Utilisateur u = getUser(auth);
        if (u == null) {
			return "redirect:/login";
		}

        try {
            Panier panier = panierService.obtenirOuCreer(u);
            Commande commande = commandeService.validerPanier(u, panier, adresse);
            ra.addFlashAttribute("success", "Commande #" + commande.getId() + " confirmée !");
            return "redirect:/commande/" + commande.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/panier/checkout";
        }
    }
}