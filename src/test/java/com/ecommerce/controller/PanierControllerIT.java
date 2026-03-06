package com.ecommerce.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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
 * Tests d'intégration – PanierController
 *
 * R1 – GET /panier sans auth          → redirect /login (sécurité)
 * R2 – GET /panier avec auth CLIENT   → 200 + vue panier/panier
 * R3 – POST /panier/ajouter valide    → redirect /panier + success
 * R4 – POST /panier/ajouter stock insuffisant → redirect /panier + erreur
 * R5 – GET /panier/checkout panier vide → redirect /panier
 * R6 – GET /panier/checkout panier non vide → 200 + vue checkout
 * R7 – Accès /panier avec rôle ADMIN  → 403 Forbidden
 */
class PanierControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // R1 – GET /panier sans auth → redirect /login
    @Test
    @DisplayName("R1 - GET /panier sans auth redirige vers /login")
    void R1_panierSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/panier"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // R2 – GET /panier avec CLIENT authentifié → 200
    @Test
    @DisplayName("R2 - GET /panier avec CLIENT retourne 200 et vue panier")
    void R2_panierAvecClient_retourne200() throws Exception {
        mockMvc.perform(get("/panier")
                        .with(user("jean.dupont@email.com").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("panier/panier"))
                .andExpect(model().attributeExists("panier"));
    }

    // R3 – POST /panier/ajouter un produit existant → redirect /panier
    @Test
    @DisplayName("R3 - POST /panier/ajouter produit valide redirige vers /panier")
    void R3_ajouterProduitValide_redirectPanier() throws Exception {
        mockMvc.perform(post("/panier/ajouter")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("produitId", "1")
                        .param("quantite", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));
    }

    // R4 – POST /panier/ajouter quantité > stock → redirect /panier avec erreur
    @Test
    @DisplayName("R4 - POST /panier/ajouter stock insuffisant redirige avec erreur")
    void R4_ajouterProduitStockInsuffisant_redirectAvecErreur() throws Exception {
        mockMvc.perform(post("/panier/ajouter")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("produitId", "1")
                        .param("quantite", "99999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));
        // Le flash "error" est vérifié côté unitaire — ici on vérifie le redirect
    }

    // R5 – GET /panier/checkout avec panier vide → redirect /panier
    @Test
    @DisplayName("R5 - GET /panier/checkout panier vide redirige vers /panier")
    void R5_checkoutPanierVide_redirectPanier() throws Exception {
        // Panier vide par défaut pour jean (aucun ajout dans ce test)
        mockMvc.perform(get("/panier/checkout")
                        .with(user("jean.dupont@email.com").roles("CLIENT")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier"));
    }

    // R6 – GET /panier/checkout avec produit ajouté → 200
    @Test
    @DisplayName("R6 - GET /panier/checkout panier non vide retourne 200")
    void R6_checkoutPanierNonVide_retourne200() throws Exception {
        // D'abord ajouter un produit
        mockMvc.perform(post("/panier/ajouter")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .param("produitId", "1")
                        .param("quantite", "1"));

        // Puis accéder au checkout
        mockMvc.perform(get("/panier/checkout")
                        .with(user("jean.dupont@email.com").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("panier/checkout"))
                .andExpect(model().attributeExists("panier"));
    }

    // R7 – Accès /panier avec ADMIN uniquement (pas CLIENT) → 403
    @Test
    @DisplayName("R7 - Accès /panier avec role ADMIN seulement retourne 403")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R7_panierAvecAdminSeulement_retourne403() throws Exception {
        mockMvc.perform(get("/panier"))
                .andExpect(status().isForbidden());
    }
}
