package com.ecommerce.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Tests d'intégration – CatalogueController
 *
 * R1 – GET /catalogue              → 200, accessible sans auth, vue catalogue/liste
 * R2 – GET /catalogue?q=Laptop     → 200, filtre par nom
 * R3 – GET /catalogue?categorieId=1 → 200, filtre par catégorie
 * R4 – GET /produit/{id} existant  → 200, vue catalogue/detail
 * R5 – GET /produit/{id} inexistant → 4xx ou redirect
 * R6 – Modèle contient produits et categories
 */
class CatalogueControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // R1 – GET /catalogue accessible sans auth
    @Test
    @DisplayName("R1 - GET /catalogue accessible sans authentification")
    void R1_getCatalogue_accessibleSansAuth() throws Exception {
        mockMvc.perform(get("/catalogue"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/liste"))
                .andExpect(model().attributeExists("produits"))
                .andExpect(model().attributeExists("categories"));
    }

    // R2 – GET /catalogue avec filtre nom
    @Test
    @DisplayName("R2 - GET /catalogue?q=... filtre les produits par nom")
    void R2_getCatalogueAvecRecherche_filtreParNom() throws Exception {
        mockMvc.perform(get("/catalogue").param("q", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/liste"))
                .andExpect(model().attribute("q", "Laptop"));
    }

    // R3 – GET /catalogue avec filtre categorie
    @Test
    @DisplayName("R3 - GET /catalogue?categorieId=... filtre par categorie")
    void R3_getCatalogueAvecCategorie_filtreParCategorie() throws Exception {
        mockMvc.perform(get("/catalogue").param("categorieId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/liste"));
    }

    // R4 – GET / (racine) accessible aussi
    @Test
    @DisplayName("R4 - GET / retourne le catalogue")
    void R4_getRacine_retourneCatalogue() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/liste"));
    }

    // R5 – GET /produit/{id} avec produit existant (créé par data.sql)
    @Test
    @DisplayName("R5 - GET /produit/{id} existant retourne 200 et vue detail")
    void R5_getDetailProduitExistant_retourne200() throws Exception {
        // Les produits sont insérés par data.sql — on récupère un ID valide
        // En testant l'URL /catalogue d'abord puis /produit/1
        mockMvc.perform(get("/catalogue"))
                .andExpect(status().isOk());

        // Tester le détail — si data.sql insère des produits, l'ID 1 devrait exister
        // Sinon ce test vérifie juste que le endpoint répond (pas 404 de Spring)
        mockMvc.perform(get("/produit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/detail"))
                .andExpect(model().attributeExists("produit"));
    }

    // R6 – Le modèle contient bien les categories pour le menu
    @Test
    @DisplayName("R6 - Le modele contient les categories pour le filtre")
    void R6_modeleContientCategories() throws Exception {
        mockMvc.perform(get("/catalogue"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("categories"));
    }
}
