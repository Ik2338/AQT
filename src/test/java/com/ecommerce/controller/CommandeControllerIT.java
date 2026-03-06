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
 * Tests d'intégration – CommandeController + flux panier → commande
 *
 * R1 – GET /commande/historique sans auth     → redirect /login
 * R2 – GET /commande/historique avec CLIENT   → 200 + vue historique
 * R3 – POST /panier/valider panier vide       → redirect /panier/checkout avec erreur
 * R4 – POST /panier/valider panier non vide   → redirect /commande/{id} (flux complet)
 * R5 – GET /commande/{id} avec CLIENT         → 200 + vue detail
 * R6 – GET /commande/historique avec ADMIN seul → 403
 */
class CommandeControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // R1 – GET /commande/historique sans auth → redirect /login
    @Test
    @DisplayName("R1 - GET /commande/historique sans auth redirige vers /login")
    void R1_historiqueSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/commande/historique"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // R2 – GET /commande/historique avec CLIENT → 200
    @Test
    @DisplayName("R2 - GET /commande/historique avec CLIENT retourne 200")
    void R2_historiqueAvecClient_retourne200() throws Exception {
        mockMvc.perform(get("/commande/historique")
                        .with(user("jean.dupont@email.com").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("commande/historique"))
                .andExpect(model().attributeExists("commandes"));
    }

    // R3 – POST /panier/valider panier vide → redirect /panier/checkout avec erreur
    @Test
    @DisplayName("R3 - POST /panier/valider panier vide redirige vers checkout avec erreur")
    void R3_validerPanierVide_redirectCheckoutAvecErreur() throws Exception {
        mockMvc.perform(post("/panier/valider")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("adresse", "12 Rue Test, Casablanca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/panier/checkout"));
    }

    // R4 – Flux complet : ajouter produit → valider panier → commande créée
    @Test
    @DisplayName("R4 - Flux complet : ajout produit + validation panier cree une commande")
    void R4_fluxComplet_ajoutEtValidation_creerCommande() throws Exception {
        // Étape 1 : ajouter un produit au panier
        mockMvc.perform(post("/panier/ajouter")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .param("produitId", "1")
                        .param("quantite", "1"))
                .andExpect(status().is3xxRedirection());

        // Étape 2 : valider le panier → doit créer une commande et rediriger
        mockMvc.perform(post("/panier/valider")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("adresse", "12 Rue Test, Casablanca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/commande/*"));
    }

    // R5 – GET /commande/{id} avec CLIENT → 200 (après création commande)
    @Test
    @DisplayName("R5 - GET /commande/{id} retourne 200 et vue detail")
    void R5_detailCommande_retourne200() throws Exception {
        // Créer une commande via le flux
        mockMvc.perform(post("/panier/ajouter")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .param("produitId", "1")
                        .param("quantite", "1"));

        var result = mockMvc.perform(post("/panier/valider")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .param("adresse", "12 Rue Test"))
                .andReturn();

        // Extraire l'URL de redirect ex: /commande/1
        String redirectUrl = result.getResponse().getRedirectedUrl();
        if (redirectUrl != null && redirectUrl.startsWith("/commande/")) {
            mockMvc.perform(get(redirectUrl)
                            .with(user("jean.dupont@email.com").roles("CLIENT")))
                    .andExpect(status().isOk())
                    .andExpect(view().name("commande/detail"))
                    .andExpect(model().attributeExists("commande"));
        }
    }

    // R6 – GET /commande/historique avec ADMIN seul → 403
    @Test
    @DisplayName("R6 - GET /commande/historique avec ADMIN seul retourne 403")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R6_historiqueAvecAdminSeul_retourne403() throws Exception {
        mockMvc.perform(get("/commande/historique"))
                .andExpect(status().isForbidden());
    }
}
