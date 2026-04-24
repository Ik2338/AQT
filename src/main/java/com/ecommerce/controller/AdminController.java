package com.ecommerce.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categorie;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.model.Produit;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private static final String REDIRECT_PRODUITS       = "redirect:/admin/produits";
    private static final String REDIRECT_CATEGORIES     = "redirect:/admin/categories";
    private static final String REDIRECT_COMMANDES      = "redirect:/admin/commandes";
    private static final String ATTR_SUCCESS            = "success";
    private static final String MODEL_CATEGORIES        = "categories";
    private static final String MODEL_NOUVELLE_CATEGORIE = "nouvelleCategorie";

    private final ProduitService   produitService;
    private final CommandeService  commandeService;
    private final CategorieService categorieService;

    public AdminController(ProduitService p, CommandeService c, CategorieService cs) {
        this.produitService   = p;
        this.commandeService  = c;
        this.categorieService = cs;
    }

    // ─── DASHBOARD ──────────────────────────────────────────────────────────

    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("nbProduits",   produitService.listerTous().size());
        model.addAttribute("nbCommandes",  commandeService.toutesLesCommandes().size());
        model.addAttribute("nbCategories", categorieService.listerToutes().size());
        return "admin/dashboard";
    }

    // ─── PRODUITS ───────────────────────────────────────────────────────────

    @GetMapping("/produits")
    public String produits(Model model) {
        model.addAttribute("produits", produitService.listerTous()); // actifs + inactifs
        return "admin/produits/liste";
    }

    @GetMapping("/produits/nouveau")
    public String nouveauProduitForm(Model model) {
        model.addAttribute("produit",        new Produit());
        model.addAttribute(MODEL_CATEGORIES, categorieService.listerToutes());
        return "admin/produits/formulaire";
    }

    @PostMapping("/produits/nouveau")
    public String creerProduit(
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam Double prix,
            @RequestParam Integer stock,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) Long categorieId,
            RedirectAttributes ra) {

        Produit p = new Produit();
        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setStock(stock);
        p.setActif(true);
        p.setImageUrl(imageUrl);
        assignerCategorie(p, categorieId);

        produitService.creer(p);
        ra.addFlashAttribute(ATTR_SUCCESS, "Produit créé avec succès.");
        return REDIRECT_PRODUITS;
    }

    @GetMapping("/produits/modifier/{id}")
    public String modifierProduitForm(@PathVariable Long id, Model model) {
        model.addAttribute("produit",        produitService.trouverParId(id));
        model.addAttribute(MODEL_CATEGORIES, categorieService.listerToutes());
        return "admin/produits/formulaire";
    }

    @PostMapping("/produits/modifier/{id}")
    public String modifierProduit(
            @PathVariable Long id,
            @RequestParam String nom,
            @RequestParam(required = false) String description,
            @RequestParam Double prix,
            @RequestParam Integer stock,
            @RequestParam(required = false) String imageUrl,
            @RequestParam(required = false) Long categorieId,
            RedirectAttributes ra) {

        Produit p = produitService.trouverParId(id);
        p.setNom(nom);
        p.setDescription(description);
        p.setPrix(prix);
        p.setStock(stock);
        p.setImageUrl(imageUrl);

        if (categorieId != null) {
            assignerCategorie(p, categorieId);
        } else {
            p.setCategorie(null);
        }

        produitService.modifier(id, p);
        ra.addFlashAttribute(ATTR_SUCCESS, "Produit modifié.");
        return REDIRECT_PRODUITS;
    }

    // ✅ NOUVEAU : Toggle actif / inactif
    @PostMapping("/produits/toggle/{id}")
    public String toggleActif(@PathVariable Long id, RedirectAttributes ra) {
        boolean nouvelEtat = produitService.toggleActif(id);
        ra.addFlashAttribute(ATTR_SUCCESS,
            nouvelEtat ? "✅ Produit activé avec succès."
                       : "⛔ Produit désactivé avec succès.");
        return REDIRECT_PRODUITS;
    }

    @PostMapping("/produits/supprimer/{id}")
    public String supprimerProduit(@PathVariable Long id, RedirectAttributes ra) {
        produitService.supprimer(id);
        ra.addFlashAttribute(ATTR_SUCCESS, "Produit supprimé.");
        return REDIRECT_PRODUITS;
    }

    @PostMapping("/produits/stock/{id}")
    public String mettreAJourStock(
            @PathVariable Long id,
            @RequestParam int stock,
            RedirectAttributes ra) {
        produitService.mettreAJourStock(id, stock);
        ra.addFlashAttribute(ATTR_SUCCESS, "Stock mis à jour.");
        return REDIRECT_PRODUITS;
    }

    // ─── CATEGORIES ─────────────────────────────────────────────────────────

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute(MODEL_CATEGORIES,          categorieService.listerToutes());
        model.addAttribute(MODEL_NOUVELLE_CATEGORIE,  new Categorie());
        return "admin/categories";
    }

    @PostMapping("/categories/nouveau")
    public String creerCategorie(@RequestParam String nom, RedirectAttributes ra) {
        if (nom != null && !nom.isBlank()) {
            categorieService.creer(nom);
            ra.addFlashAttribute(ATTR_SUCCESS, "Catégorie créée.");
        }
        return REDIRECT_CATEGORIES;
    }

    @PostMapping("/categories/supprimer/{id}")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes ra) {
        categorieService.supprimer(id);
        ra.addFlashAttribute(ATTR_SUCCESS, "Catégorie supprimée.");
        return REDIRECT_CATEGORIES;
    }

    // ─── COMMANDES ──────────────────────────────────────────────────────────

    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("commandes", commandeService.toutesLesCommandes());
        model.addAttribute("etats",     EtatCommande.values());
        return "admin/commandes";
    }

    @PostMapping("/commandes/{id}/etat")
    public String changerEtat(
            @PathVariable Long id,
            @RequestParam EtatCommande etat,
            RedirectAttributes ra) {
        commandeService.changerEtat(id, etat);
        ra.addFlashAttribute(ATTR_SUCCESS, "État mis à jour.");
        return REDIRECT_COMMANDES;
    }

    // ─── MÉTHODES PRIVÉES ───────────────────────────────────────────────────

    private void assignerCategorie(Produit p, Long categorieId) {
        if (categorieId == null) return;
        try {
            p.setCategorie(categorieService.trouverParId(categorieId));
        } catch (ResourceNotFoundException _) {
            log.warn("Catégorie {} introuvable, produit sans catégorie assignée.", categorieId);
        }
    }
}