package com.ecommerce.controller;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecommerce.service.CommandeService;
import com.ecommerce.service.UtilisateurService;

@Controller @RequestMapping("/commande")
public class CommandeController {
    private final CommandeService commandeService;
    private final UtilisateurService utilisateurService;
    public CommandeController(CommandeService c, UtilisateurService u) { this.commandeService=c; this.utilisateurService=u; }

    @GetMapping("/historique")
    public String historique(Authentication auth, Model model) {
        var u = utilisateurService.trouverParEmail(auth.getName());
        model.addAttribute("commandes", commandeService.historiqueClient(u.getId()));
        return "commande/historique";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("commande", commandeService.trouverParId(id));
        return "commande/detail";
    }
}
