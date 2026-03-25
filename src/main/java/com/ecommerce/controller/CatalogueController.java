package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.ProduitService;

@Controller
public class CatalogueController {

    private final ProduitService produitService;
    private final CategorieService categorieService;

    // Injection des services via constructeur
    public CatalogueController(ProduitService produitService, CategorieService categorieService) {
        this.produitService = produitService;
        this.categorieService = categorieService;
    }

    // Affiche la liste des produits avec recherche par mot-clé et/ou catégorie
    @GetMapping({"/", "/catalogue"})
    public String catalogue(@RequestParam(required = false) String q,
                            @RequestParam(required = false) Long categorieId,
                            Model model) {
        model.addAttribute("produits", produitService.rechercher(q, categorieId));
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("q", q);                     // Conserve le mot-clé dans le formulaire
        model.addAttribute("categorieId", categorieId); // Conserve la catégorie sélectionnée
        return "catalogue/liste";
    }

    // Affiche la page de détail d'un produit par son ID
    @GetMapping("/produit/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("produit", produitService.trouverParId(id));
        model.addAttribute("categories", categorieService.listerToutes());
        return "catalogue/detail";
    }
}