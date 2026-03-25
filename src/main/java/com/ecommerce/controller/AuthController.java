package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

@Controller
public class AuthController {

    private final UtilisateurService utilisateurService;

    // Injection du service utilisateur via constructeur
    public AuthController(UtilisateurService utilisateurService) {
        this.utilisateurService = utilisateurService;
    }

    // Affiche la page de login, avec message d'erreur si échec d'authentification
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("error", "Email ou mot de passe incorrect.");
        }
        return "auth/login";
    }

    // Affiche le formulaire d'inscription avec un objet Utilisateur vide
    @GetMapping("/inscription")
    public String inscriptionForm(Model model) {
        model.addAttribute("utilisateur", new Utilisateur());
        return "auth/inscription";
    }

    // Traite la soumission du formulaire d'inscription
    @PostMapping("/inscription")
    public String inscrire(@ModelAttribute Utilisateur utilisateur, RedirectAttributes ra) {
        try {
            utilisateurService.inscrire(utilisateur);
            ra.addFlashAttribute("success", "Compte créé ! Connectez-vous.");
            return "redirect:/login";
        } catch (Exception e) {
            // En cas d'erreur (ex: email déjà utilisé), redirige avec le message
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inscription";
        }
    }
}