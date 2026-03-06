package com.ecommerce.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration – AdminController
 *
 * Sécurité :
 * R1  – GET /admin sans auth          → redirect /login
 * R2  – GET /admin avec CLIENT seul   → 403
 * R3  – GET /admin avec ADMIN         → 200 dashboard
 *
 * Produits :
 * R4  – GET /admin/produits           → 200 + liste produits
 * R5  – GET /admin/produits/nouveau   → 200 + formulaire vide
 * R6  – POST /admin/produits/nouveau  → redirect /admin/produits
 * R7  – POST /admin/produits/supprimer/{id} → redirect /admin/produits
 *
 * Catégories :
 * R8  – GET /admin/categories         → 200 + liste categories
 * R9  – POST /admin/categories/nouveau → redirect /admin/categories
 * R10 – POST /admin/categories/supprimer/{id} → redirect /admin/categories
 *
 * Commandes :
 * R11 – GET /admin/commandes          → 200 + liste commandes
 * R12 – POST /admin/commandes/{id}/etat → redirect /admin/commandes
 */
class AdminControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // ─── SÉCURITÉ ────────────────────────────────────────────────

    // R1 – GET /admin sans auth → redirect /login
    @Test
    @DisplayName("R1 - GET /admin sans auth redirige vers /login")
    void R1_adminSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // R2 – GET /admin avec CLIENT seul → 403
    @Test
    @DisplayName("R2 - GET /admin avec CLIENT seul retourne 403")
    @WithMockUser(username = "jean.dupont@email.com", roles = {"CLIENT"})
    void R2_adminAvecClientSeul_retourne403() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    // R3 – GET /admin avec ADMIN → 200 dashboard
    @Test
    @DisplayName("R3 - GET /admin avec ADMIN retourne 200 et dashboard")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R3_adminAvecAdmin_retourneDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("nbProduits"))
                .andExpect(model().attributeExists("nbCommandes"))
                .andExpect(model().attributeExists("nbCategories"));
    }

    // ─── PRODUITS ────────────────────────────────────────────────

    // R4 – GET /admin/produits → 200
    @Test
    @DisplayName("R4 - GET /admin/produits retourne 200 et liste des produits")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R4_adminProduits_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/produits"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/produits/liste"))
                .andExpect(model().attributeExists("produits"));
    }

    // R5 – GET /admin/produits/nouveau → formulaire vide
    @Test
    @DisplayName("R5 - GET /admin/produits/nouveau retourne le formulaire vide")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R5_adminNouveauProduitForm_retourneFormulaire() throws Exception {
        mockMvc.perform(get("/admin/produits/nouveau"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/produits/formulaire"))
                .andExpect(model().attributeExists("produit"))
                .andExpect(model().attributeExists("categories"));
    }

    // R6 – POST /admin/produits/nouveau → redirect /admin/produits
    @Test
    @DisplayName("R6 - POST /admin/produits/nouveau cree produit et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R6_adminCreerProduit_redirectListeProduits() throws Exception {
        mockMvc.perform(post("/admin/produits/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Nouveau Produit Test")
                        .param("description", "Description test")
                        .param("prix", "299.99")
                        .param("stock", "20"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    // R7 – POST /admin/produits/supprimer/{id} → soft-delete + redirect
    @Test
    @DisplayName("R7 - POST /admin/produits/supprimer/{id} desactive et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R7_adminSupprimerProduit_redirectListeProduits() throws Exception {
        mockMvc.perform(post("/admin/produits/supprimer/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    // ─── CATÉGORIES ──────────────────────────────────────────────

    // R8 – GET /admin/categories → 200
    @Test
    @DisplayName("R8 - GET /admin/categories retourne 200 et la liste")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R8_adminCategories_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

    // R9 – POST /admin/categories/nouveau → redirect /admin/categories
    @Test
    @DisplayName("R9 - POST /admin/categories/nouveau cree categorie et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R9_adminCreerCategorie_redirectListeCategories() throws Exception {
        mockMvc.perform(post("/admin/categories/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Nouvelle Categorie Test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    // R10 – POST /admin/categories/supprimer/{id} → redirect /admin/categories
    @Test
    @DisplayName("R10 - POST /admin/categories/supprimer/{id} supprime et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R10_adminSupprimerCategorie_redirectListeCategories() throws Exception {
        // D'abord créer une catégorie pour la supprimer
        mockMvc.perform(post("/admin/categories/nouveau")
                        .with(csrf())
                        .param("nom", "CatASupprimer"));

        // Récupérer l'ID de la catégorie créée via GET /admin/categories
        // et la supprimer — ici on utilise un ID connu du data.sql si présent
        mockMvc.perform(post("/admin/categories/supprimer/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    // ─── COMMANDES ───────────────────────────────────────────────

    // R11 – GET /admin/commandes → 200
    @Test
    @DisplayName("R11 - GET /admin/commandes retourne 200 et la liste des commandes")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R11_adminCommandes_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/commandes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commandes"))
                .andExpect(model().attributeExists("commandes"));
    }

    // R12 – POST /admin/commandes/{id}/etat → redirect après création d'une commande
    @Test
    @DisplayName("R12 - POST /admin/commandes/{id}/etat change l'etat et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN", "CLIENT"})
    void R12_adminChangerEtatCommande_redirect() throws Exception {
        // Créer une commande via le flux panier
        mockMvc.perform(post("/panier/ajouter")
                        .with(csrf())
                        .param("produitId", "1")
                        .param("quantite", "1"));

        var result = mockMvc.perform(post("/panier/valider")
                        .with(csrf())
                        .param("adresse", "Test"))
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        if (redirectUrl != null && redirectUrl.startsWith("/commande/")) {
            String commandeId = redirectUrl.replace("/commande/", "");
            mockMvc.perform(post("/admin/commandes/" + commandeId + "/etat")
                            .with(csrf())
                            .param("etat", "EN_COURS"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/commandes"));
        }
    }
}
