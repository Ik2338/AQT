package com.ecommerce.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecommerce.repository.CategorieRepository;
import com.ecommerce.service.ProduitService;

@Controller
public class CatalogueController {
    private final ProduitService produitService;
    private final CategorieRepository categorieRepo;
    public CatalogueController(ProduitService produitService, CategorieRepository categorieRepo) {
        this.produitService=produitService; this.categorieRepo=categorieRepo;
    }

    @GetMapping({"/", "/catalogue"})
    public String catalogue(@RequestParam(required=false) String q,
                            @RequestParam(required=false) Long categorieId, Model model) {
        model.addAttribute("produits", produitService.rechercher(q, categorieId));
        model.addAttribute("categories", categorieRepo.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categorieId", categorieId);
        return "catalogue/liste";
    }

    @GetMapping("/produit/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("produit", produitService.trouverParId(id));
        model.addAttribute("categories", categorieRepo.findAll());
        return "catalogue/detail";
    }
}
