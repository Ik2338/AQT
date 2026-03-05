package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.model.Categorie;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.CategorieRepository;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProduitService produitService;
    private final CommandeService commandeService;
    private final CategorieRepository categorieRepo;

    public AdminController(ProduitService p, CommandeService c, CategorieRepository cr) {
        this.produitService = p;
        this.commandeService = c;
        this.categorieRepo = cr;
    }

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("nbProduits", produitService.listerTous().size());
        model.addAttribute("nbCommandes", commandeService.toutesLesCommandes().size());
        model.addAttribute("nbCategories", categorieRepo.findAll().size());
        return "admin/dashboard";
    }

    // ─── PRODUITS ───────────────────────────────────────────
    @GetMapping("/produits")
    public String produits(Model model) {
        model.addAttribute("produits", produitService.listerTous());
        return "admin/produits/liste";
    }

    @GetMapping("/produits/nouveau")
    public String nouveauProduitForm(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("categories", categorieRepo.findAll());
        return "admin/produits/formulaire";
    }

    @PostMapping("/produits/nouveau")
    public String creerProduit(
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam Double prix,
            @RequestParam Integer stock,
            @RequestParam(required = false) Long categorieId,
            RedirectAttributes ra) {
        Produit p = new Produit();
        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setStock(stock);
        p.setActif(true);
        if (categorieId != null) {
            categorieRepo.findById(categorieId).ifPresent(p::setCategorie);
        }
        produitService.creer(p);
        ra.addFlashAttribute("success", "Produit créé avec succès.");
        return "redirect:/admin/produits";
    }

    @GetMapping("/produits/modifier/{id}")
    public String modifierProduitForm(@PathVariable Long id, Model model) {
        model.addAttribute("produit", produitService.trouverParId(id));
        model.addAttribute("categories", categorieRepo.findAll());
        return "admin/produits/formulaire";
    }

    @PostMapping("/produits/modifier/{id}")
    public String modifierProduit(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam Double prix,
            @RequestParam Integer stock,
            @RequestParam(required = false) Long categorieId,
            RedirectAttributes ra) {
        Produit p = produitService.trouverParId(id);
        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setStock(stock);
        if (categorieId != null) {
            categorieRepo.findById(categorieId).ifPresent(p::setCategorie);
        } else {
            p.setCategorie(null);
        }
        produitService.modifier(id, p);
        ra.addFlashAttribute("success", "Produit modifié.");
        return "redirect:/admin/produits";
    }

    @PostMapping("/produits/supprimer/{id}")
    public String supprimerProduit(@PathVariable Long id, RedirectAttributes ra) {
        produitService.supprimer(id);
        ra.addFlashAttribute("success", "Produit supprimé.");
        return "redirect:/admin/produits";
    }

    @PostMapping("/produits/stock/{id}")
    public String mettreAJourStock(@PathVariable Long id,
                                    @RequestParam int stock,
                                    RedirectAttributes ra) {
        produitService.mettreAJourStock(id, stock);
        ra.addFlashAttribute("success", "Stock mis à jour.");
        return "redirect:/admin/produits";
    }

    // ─── CATEGORIES ─────────────────────────────────────────
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categorieRepo.findAll());
        model.addAttribute("nouvelleCategorie", new Categorie());
        return "admin/categories";
    }

    @PostMapping("/categories/nouveau")
    public String creerCategorie(@RequestParam String nom, RedirectAttributes ra) {
        if (nom != null && !nom.isBlank()) {
            Categorie cat = new Categorie();
            cat.setNom(nom.trim());
            categorieRepo.save(cat);
            ra.addFlashAttribute("success", "Catégorie créée.");
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/supprimer/{id}")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes ra) {
        categorieRepo.deleteById(id);
        ra.addFlashAttribute("success", "Catégorie supprimée.");
        return "redirect:/admin/categories";
    }

    // ─── COMMANDES ──────────────────────────────────────────
    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("commandes", commandeService.toutesLesCommandes());
        model.addAttribute("etats", EtatCommande.values());
        return "admin/commandes";
    }

    @PostMapping("/commandes/{id}/etat")
    public String changerEtat(@PathVariable Long id,
                               @RequestParam EtatCommande etat,
                               RedirectAttributes ra) {
        commandeService.changerEtat(id, etat);
        ra.addFlashAttribute("success", "État mis à jour.");
        return "redirect:/admin/commandes";
    }
}