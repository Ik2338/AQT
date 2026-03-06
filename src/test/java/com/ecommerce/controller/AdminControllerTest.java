package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.ecommerce.model.Categorie;
import com.ecommerce.model.Commande;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;

/**
 * Tests unitaires – AdminController
 * @ExtendWith(MockitoExtension.class) — PAS @SpringBootTest
 * Spring ne démarre PAS, pas de conflit de mapping
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock private ProduitService produitService;
    @Mock private CommandeService commandeService;
    @Mock private CategorieService categorieService;
    @InjectMocks private AdminController adminController;

    private Model model;
    private RedirectAttributesModelMap ra;
    private Produit produit;
    private Utilisateur utilisateur;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        ra = new RedirectAttributesModelMap();
        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setId(1L);
        utilisateur = new Utilisateur("Admin", "System", "admin@ecommerce.com", "pass");
    }

    @Test
    @DisplayName("R1 - dashboard affiche nbProduits, nbCommandes, nbCategories")
    void R1_dashboard_afficheCompteurs() {
        when(produitService.listerTous()).thenReturn(List.of(produit));
        when(commandeService.toutesLesCommandes()).thenReturn(List.of());
        when(categorieService.listerToutes()).thenReturn(List.of(new Categorie("Elec")));

        String vue = adminController.dashboard(model);

        assertThat(vue).isEqualTo("admin/dashboard");
        assertThat(model.getAttribute("nbProduits")).isEqualTo(1);
        assertThat(model.getAttribute("nbCommandes")).isEqualTo(0);
        assertThat(model.getAttribute("nbCategories")).isEqualTo(1);
    }

    @Test
    @DisplayName("R2 - liste produits retourne la vue avec les produits")
    void R2_listeProduits_retourneVueAvecProduits() {
        when(produitService.listerTous()).thenReturn(List.of(produit));

        String vue = adminController.produits(model);

        assertThat(vue).isEqualTo("admin/produits/liste");
        assertThat((List<?>) model.getAttribute("produits")).hasSize(1);
    }

    @Test
    @DisplayName("R3 - formulaire nouveau produit retourne un produit vide")
    void R3_nouveauProduitForm_retourneFormulaireVide() {
        when(categorieService.listerToutes()).thenReturn(List.of());

        String vue = adminController.nouveauProduitForm(model);

        assertThat(vue).isEqualTo("admin/produits/formulaire");
        assertThat(model.getAttribute("produit")).isInstanceOf(Produit.class);
        assertThat(((Produit) model.getAttribute("produit")).getId()).isNull();
    }

    @Test
    @DisplayName("R4 - creer produit sauvegarde et redirige")
    void R4_creerProduit_sauvegardeEtRedirect() {
        when(produitService.creer(any())).thenReturn(produit);

        String vue = adminController.creerProduit("Laptop", "Desc", 999.0, 10, null, ra);

        assertThat(vue).isEqualTo("redirect:/admin/produits");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Produit créé avec succès.");
        verify(produitService).creer(any());
    }

    @Test
    @DisplayName("R5 - formulaire modifier produit retourne le produit existant")
    void R5_modifierProduitForm_retourneFormulaireAvecProduit() {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(categorieService.listerToutes()).thenReturn(List.of());

        String vue = adminController.modifierProduitForm(1L, model);

        assertThat(vue).isEqualTo("admin/produits/formulaire");
        assertThat(model.getAttribute("produit")).isEqualTo(produit);
    }

    @Test
    @DisplayName("R6 - modifier produit met a jour et redirige")
    void R6_modifierProduit_metAJourEtRedirect() {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(produitService.modifier(anyLong(), any())).thenReturn(produit);

        String vue = adminController.modifierProduit(1L, "NewName", "NewDesc", 1200.0, 5, null, ra);

        assertThat(vue).isEqualTo("redirect:/admin/produits");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Produit modifié.");
        verify(produitService).modifier(eq(1L), any());
    }

    @Test
    @DisplayName("R7 - supprimer produit desactive et redirige")
    void R7_supprimerProduit_desactiveEtRedirect() {
        doNothing().when(produitService).supprimer(1L);

        String vue = adminController.supprimerProduit(1L, ra);

        assertThat(vue).isEqualTo("redirect:/admin/produits");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Produit supprimé.");
        verify(produitService).supprimer(1L);
    }

    @Test
    @DisplayName("R8 - mise a jour stock met a jour et redirige")
    void R8_mettreAJourStock_metAJourEtRedirect() {
        doNothing().when(produitService).mettreAJourStock(1L, 50);

        String vue = adminController.mettreAJourStock(1L, 50, ra);

        assertThat(vue).isEqualTo("redirect:/admin/produits");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Stock mis à jour.");
        verify(produitService).mettreAJourStock(1L, 50);
    }

    @Test
    @DisplayName("R9 - liste categories retourne la vue avec les categories")
    void R9_listeCategories_retourneVueAvecCategories() {
        when(categorieService.listerToutes()).thenReturn(List.of(new Categorie("Elec")));

        String vue = adminController.categories(model);

        assertThat(vue).isEqualTo("admin/categories");
        assertThat((List<?>) model.getAttribute("categories")).hasSize(1);
    }

    @Test
    @DisplayName("R10 - creer categorie sauvegarde et redirige")
    void R10_creerCategorie_sauvegardeEtRedirect() {
        when(categorieService.creer("Sport")).thenReturn(new Categorie("Sport"));

        String vue = adminController.creerCategorie("Sport", ra);

        assertThat(vue).isEqualTo("redirect:/admin/categories");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Catégorie créée.");
        verify(categorieService).creer("Sport");
    }

    @Test
    @DisplayName("R11 - supprimer categorie supprime et redirige")
    void R11_supprimerCategorie_supprimeEtRedirect() {
        doNothing().when(categorieService).supprimer(1L);

        String vue = adminController.supprimerCategorie(1L, ra);

        assertThat(vue).isEqualTo("redirect:/admin/categories");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Catégorie supprimée.");
        verify(categorieService).supprimer(1L);
    }

    @Test
    @DisplayName("R12 - liste commandes retourne toutes les commandes")
    void R12_listeCommandes_retourneVueAvecCommandes() {
        when(commandeService.toutesLesCommandes()).thenReturn(List.of(new Commande(utilisateur)));

        String vue = adminController.commandes(model);

        assertThat(vue).isEqualTo("admin/commandes");
        assertThat((List<?>) model.getAttribute("commandes")).hasSize(1);
    }

    @Test
    @DisplayName("R13 - changer etat commande met a jour et redirige")
    void R13_changerEtatCommande_metAJourEtRedirect() {
        Commande commande = new Commande(utilisateur);
        when(commandeService.changerEtat(1L, EtatCommande.VALIDEE)).thenReturn(commande);

        String vue = adminController.changerEtat(1L, EtatCommande.VALIDEE, ra);

        assertThat(vue).isEqualTo("redirect:/admin/commandes");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("État mis à jour.");
        verify(commandeService).changerEtat(1L, EtatCommande.VALIDEE);
    }
}