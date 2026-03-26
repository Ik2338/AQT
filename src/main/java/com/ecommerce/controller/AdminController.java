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
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProduitService produitService;
    private final CommandeService commandeService;
    private final CategorieService categorieService;

    // Injection des services via constructeur
    public AdminController(ProduitService p, CommandeService c, CategorieService cs) {
        this.produitService = p;
        this.commandeService = c;
        this.categorieService = cs;
    }

    // Tableau de bord : affiche les statistiques globales
    @GetMapping({"", "/"})
    public String dashboard(Model model) {
        model.addAttribute("nbProduits", produitService.listerTous().size());
        model.addAttribute("nbCommandes", commandeService.toutesLesCommandes().size());
        model.addAttribute("nbCategories", categorieService.listerToutes().size());
        return "admin/dashboard";
    }

    // ─── PRODUITS ───────────────────────────────────────────

    // Liste tous les produits
    @GetMapping("/produits")
    public String produits(Model model) {
        model.addAttribute("produits", produitService.listerTous());
        return "admin/produits/liste";
    }

    // Affiche le formulaire de création d'un produit
    @GetMapping("/produits/nouveau")
    public String nouveauProduitForm(Model model) {
        model.addAttribute("produit", new Produit());
        model.addAttribute("categories", categorieService.listerToutes());
        return "admin/produits/formulaire";
    }

    // Traite la soumission du formulaire de création
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
        // Associe la catégorie si fournie
        if (categorieId != null) {
            try { p.setCategorie(categorieService.trouverParId(categorieId)); }
            catch (Exception ignored) {}
        }
        produitService.creer(p);
        ra.addFlashAttribute("success", "Produit créé avec succès.");
        return "redirect:/admin/produits";
    }

    // Affiche le formulaire de modification d'un produit existant
    @GetMapping("/produits/modifier/{id}")
    public String modifierProduitForm(@PathVariable Long id, Model model) {
        model.addAttribute("produit", produitService.trouverParId(id));
        model.addAttribute("categories", categorieService.listerToutes());
        return "admin/produits/formulaire";
    }

    // Traite la soumission du formulaire de modification
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
            try { p.setCategorie(categorieService.trouverParId(categorieId)); }
            catch (Exception ignored) {}
        } else {
            p.setCategorie(null); // Retire la catégorie si non sélectionnée
        }
        produitService.modifier(id, p);
        ra.addFlashAttribute("success", "Produit modifié.");
        return "redirect:/admin/produits";
    }

    // Supprime un produit par son ID
    @PostMapping("/produits/supprimer/{id}")
    public String supprimerProduit(@PathVariable Long id, RedirectAttributes ra) {
        produitService.supprimer(id);
        ra.addFlashAttribute("success", "Produit supprimé.");
        return "redirect:/admin/produits";
    }

    // Met à jour uniquement le stock d'un produit
    @PostMapping("/produits/stock/{id}")
    public String mettreAJourStock(@PathVariable Long id,
                                    @RequestParam int stock,
                                    RedirectAttributes ra) {
        produitService.mettreAJourStock(id, stock);
        ra.addFlashAttribute("success", "Stock mis à jour.");
        return "redirect:/admin/produits";
    }

    // ─── CATEGORIES ─────────────────────────────────────────

    // Liste toutes les catégories avec formulaire d'ajout
    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", categorieService.listerToutes());
        model.addAttribute("nouvelleCategorie", new Categorie());
        return "admin/categories";
    }

    // Crée une nouvelle catégorie si le nom n'est pas vide
    @PostMapping("/categories/nouveau")
    public String creerCategorie(@RequestParam String nom, RedirectAttributes ra) {
        if (nom != null && !nom.isBlank()) {
            categorieService.creer(nom);
            ra.addFlashAttribute("success", "Catégorie créée.");
        }
        return "redirect:/admin/categories";
    }

    // Supprime une catégorie par son ID
    @PostMapping("/categories/supprimer/{id}")
    public String supprimerCategorie(@PathVariable Long id, RedirectAttributes ra) {
        categorieService.supprimer(id);
        ra.addFlashAttribute("success", "Catégorie supprimée.");
        return "redirect:/admin/categories";
    }

    // ─── COMMANDES ──────────────────────────────────────────

    // Liste toutes les commandes avec les états disponibles
    @GetMapping("/commandes")
    public String commandes(Model model) {
        model.addAttribute("commandes", commandeService.toutesLesCommandes());
        model.addAttribute("etats", EtatCommande.values());
        return "admin/commandes";
    }

    // Met à jour l'état d'une commande (ex: EN_ATTENTE → EXPEDIEE)
    @PostMapping("/commandes/{id}/etat")
    public String changerEtat(@PathVariable Long id,
                               @RequestParam EtatCommande etat,
                               RedirectAttributes ra) {
        commandeService.changerEtat(id, etat);
        ra.addFlashAttribute("success", "État mis à jour.");
        return "redirect:/admin/commandes";
    }
}