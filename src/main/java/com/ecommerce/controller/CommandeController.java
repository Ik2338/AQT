package com.ecommerce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.UtilisateurService;

@Controller
@RequestMapping("/commande")
public class CommandeController {

    private final CommandeService commandeService;
    private final UtilisateurService utilisateurService;

    public CommandeController(CommandeService c, UtilisateurService u) {
        this.commandeService = c;
        this.utilisateurService = u;
    }

    // Méthode utilitaire pour récupérer l'utilisateur connecté
    private Utilisateur getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return utilisateurService.trouverParEmailOptional(auth.getName()).orElse(null);
    }

    @GetMapping("/historique")
    public String historique(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        if (u == null) {
            return "redirect:/login";
        }
        model.addAttribute("commandes", commandeService.historiqueClient(u.getId()));
        return "commande/historique";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("commande", commandeService.trouverParId(id));
        return "commande/detail";
    }
}