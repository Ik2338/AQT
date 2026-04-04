package com.ecommerce.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

@Controller
@RequestMapping("/profil")
public class ProfilController {

    private final UtilisateurService utilisateurService;

    public ProfilController(UtilisateurService u) {
        this.utilisateurService = u;
    }

    // Méthode utilitaire pour récupérer l'utilisateur connecté
    private Utilisateur getUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getName())) {
            return null;
        }
        return utilisateurService.trouverParEmailOptional(auth.getName()).orElse(null);
    }

    @GetMapping
    public String profil(Authentication auth, Model model) {
        Utilisateur u = getUser(auth);
        if (u == null) {
            return "redirect:/login";
        }
        model.addAttribute("utilisateur", u);
        return "profil/profil";
    }

    @PostMapping("/modifier")
    public String modifier(@RequestParam String nom,
                           @RequestParam String prenom,
                           @RequestParam(required = false) String telephone,
                           @RequestParam(required = false) String adresse,
                           Authentication auth, RedirectAttributes ra) {
        Utilisateur u = getUser(auth);
        if (u == null) {
            ra.addFlashAttribute("error", "Veuillez vous connecter");
            return "redirect:/login";
        }
        utilisateurService.mettreAJourProfil(u.getId(), nom, prenom, telephone, adresse);
        ra.addFlashAttribute("success", "Profil mis à jour !");
        return "redirect:/profil";
    }
}