package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.config.SecurityConfigTest;
import com.ecommerce.model.Categorie;
import com.ecommerce.model.Commande;
import com.ecommerce.model.Produit;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;

@WebMvcTest(AdminController.class)
@Import(SecurityConfigTest.class)
class AdminControllerTest {

    @Autowired MockMvc mvc;

    @MockBean ProduitService   produitService;
    @MockBean CommandeService  commandeService;
    @MockBean CategorieService categorieService;

    private Produit   produit;
    private Categorie categorie;
    private Commande  commande;

    @BeforeEach
    void setUp() {
        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setId(1L);
        produit.setActif(true);

        categorie = new Categorie("Electronique");
        categorie.setId(1L);

        commande = new Commande();
        commande.setId(1L);
        commande.setEtat(Commande.EtatCommande.EN_COURS);
    }

    @Test
    @DisplayName("R1 - Dashboard accessible")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R1_dashboard_accessible() throws Exception {
        when(produitService.listerTous()).thenReturn(List.of(produit));
        when(commandeService.toutesLesCommandes()).thenReturn(List.of(commande));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/admin"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/dashboard"))
           .andExpect(model().attributeExists("nbProduits", "nbCommandes", "nbCategories"));
    }

    @Test
    @DisplayName("R2 - Liste produits affichee")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R2_listeProduits_affichee() throws Exception {
        when(produitService.listerTous()).thenReturn(List.of(produit));

        mvc.perform(get("/admin/produits"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/produits/liste"))
           .andExpect(model().attributeExists("produits"));
    }

    @Test
    @DisplayName("R3 - Creer produit redirige vers liste")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R3_creerProduit_redirige() throws Exception {
        when(produitService.creer(any())).thenReturn(produit);

        mvc.perform(post("/admin/produits/nouveau")
           .with(csrf())
           .param("nom", "Laptop")
           .param("prix", "999.99")
           .param("stock", "10"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R4 - Modifier produit redirige vers liste")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R4_modifierProduit_redirige() throws Exception {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(produitService.modifier(anyLong(), any())).thenReturn(produit);

        mvc.perform(post("/admin/produits/modifier/1")
           .with(csrf())
           .param("nom", "Laptop Pro")
           .param("prix", "1299.99")
           .param("stock", "5"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R5 - Supprimer produit redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R5_supprimerProduit_redirige() throws Exception {
        doNothing().when(produitService).supprimer(1L);

        mvc.perform(post("/admin/produits/supprimer/1")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));

        verify(produitService).supprimer(1L);
    }

    @Test
    @DisplayName("R6 - Liste categories affichee")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R6_listeCategories_affichee() throws Exception {
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/admin/categories"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/categories"))
           .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("R7 - Creer categorie redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R7_creerCategorie_redirige() throws Exception {
        when(categorieService.creer("Sport")).thenReturn(categorie);

        mvc.perform(post("/admin/categories/nouveau")
           .with(csrf())
           .param("nom", "Sport"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/categories"));
    }

    @Test
    @DisplayName("R8 - Liste commandes affichee")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R8_listeCommandes_affichee() throws Exception {
        when(commandeService.toutesLesCommandes()).thenReturn(List.of(commande));

        mvc.perform(get("/admin/commandes"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/commandes"))
           .andExpect(model().attributeExists("commandes"));
    }

    @Test
    @DisplayName("R9 - Changer etat commande redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R9_changerEtat_redirige() throws Exception {
        when(commandeService.changerEtat(anyLong(), any())).thenReturn(commande);

        mvc.perform(post("/admin/commandes/1/etat")
           .with(csrf())
           .param("etat", "VALIDEE"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/commandes"));
    }
 // Ajouter à la fin de AdminControllerTest, avant la dernière accolade

    @Test
    @DisplayName("R10 - Modifier produit avec categorieId valide")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R10_modifierProduitAvecCategorie_redirige() throws Exception {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(categorieService.trouverParId(1L)).thenReturn(categorie);
        when(produitService.modifier(anyLong(), any())).thenReturn(produit);

        mvc.perform(post("/admin/produits/modifier/1")
           .with(csrf())
           .param("nom", "Laptop Pro")
           .param("prix", "1299.99")
           .param("stock", "5")
           .param("categorieId", "1"))   // ← branche categorieId != null couverte
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R11 - Modifier produit sans categorieId met categorie a null")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R11_modifierProduitSansCategorie_setCategorieNull() throws Exception {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(produitService.modifier(anyLong(), any())).thenReturn(produit);

        mvc.perform(post("/admin/produits/modifier/1")
           .with(csrf())
           .param("nom", "Laptop")
           .param("prix", "999.99")
           .param("stock", "10"))        // ← pas de categorieId → branche else couverte
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R12 - Mettre a jour stock redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R12_mettreAJourStock_redirige() throws Exception {
        mvc.perform(post("/admin/produits/stock/1")
           .with(csrf())
           .param("stock", "50"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/produits"));

        verify(produitService).mettreAJourStock(1L, 50);
    }

    @Test
    @DisplayName("R13 - Supprimer categorie redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R13_supprimerCategorie_redirige() throws Exception {
        mvc.perform(post("/admin/categories/supprimer/1")
           .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/categories"));

        verify(categorieService).supprimer(1L);
    }

    @Test
    @DisplayName("R14 - Creer categorie avec nom vide ne cree pas et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R14_creerCategorieNomVide_neCreePassEtRedirige() throws Exception {
        mvc.perform(post("/admin/categories/nouveau")
           .with(csrf())
           .param("nom", "   "))         // ← branche isBlank() couverte
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/admin/categories"));

        verify(categorieService, org.mockito.Mockito.never()).creer(any());
    }

    @Test
    @DisplayName("R15 - Formulaire modification produit retourne vue formulaire")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R15_modifierProduitForm_retourneFormulaire() throws Exception {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/admin/produits/modifier/1"))
           .andExpect(status().isOk())
           .andExpect(view().name("admin/produits/formulaire"))
           .andExpect(model().attributeExists("produit", "categories"));
    }
}