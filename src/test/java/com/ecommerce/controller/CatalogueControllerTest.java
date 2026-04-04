package com.ecommerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.config.SecurityConfigTest;
import com.ecommerce.model.Categorie;
import com.ecommerce.model.Produit;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.ProduitService;

@WebMvcTest(CatalogueController.class)
@Import(SecurityConfigTest.class)
class CatalogueControllerTest {

    @Autowired MockMvc mvc;
    @MockBean ProduitService   produitService;
    @MockBean CategorieService categorieService;

    private Produit   produit;
    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie("Electronique");
        categorie.setId(1L);
        produit = new Produit("Laptop", "Desc", 999.0, 10, categorie);
        produit.setId(1L);
        produit.setActif(true);
    }

    // R1 – Catalogue affiche liste produits
    // /catalogue est dans les URLs publiques de SecurityConfigTest → pas besoin de @WithMockUser
    @Test
    @DisplayName("R1 - Catalogue affiche liste produits")
    void R1_catalogue_afficheListe() throws Exception {
        when(produitService.rechercher(null, null)).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/catalogue"))
           .andExpect(status().isOk())
           .andExpect(view().name("catalogue/liste"))
           .andExpect(model().attributeExists("produits", "categories"));
    }

    // R2 – Recherche par mot-clé
    @Test
    @DisplayName("R2 - Recherche par mot-cle fonctionne")
    void R2_catalogue_rechercheParMotCle() throws Exception {
        when(produitService.rechercher("Laptop", null)).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/catalogue").param("q", "Laptop"))
           .andExpect(status().isOk())
           .andExpect(model().attribute("q", "Laptop"));
    }

    // R3 – Filtre par catégorie
    @Test
    @DisplayName("R3 - Filtre par categorie fonctionne")
    void R3_catalogue_filtreParCategorie() throws Exception {
        when(produitService.rechercher(null, 1L)).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/catalogue").param("categorieId", "1"))
           .andExpect(status().isOk())
           .andExpect(model().attribute("categorieId", 1L));
    }

    // R4 – Détail produit affiché
    // /produit/** est dans les URLs publiques de SecurityConfigTest
    @Test
    @DisplayName("R4 - Detail produit affiche")
    void R4_detail_afficheProduit() throws Exception {
        when(produitService.trouverParId(1L)).thenReturn(produit);
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/produit/1"))
           .andExpect(status().isOk())
           .andExpect(view().name("catalogue/detail"))
           .andExpect(model().attributeExists("produit"));
    }

    // R5 – Modèle contient catégories
    // CORRECTION : on appelle andExpect(status().isOk()) AVANT model() pour que
    // MockMvc ne lève pas "No ModelAndView found" (la requête doit aboutir avant
    // qu'on inspecte le modèle)
    @Test
    @DisplayName("R5 - Modele contient les categories")
    void R5_catalogue_modeleContientCategories() throws Exception {
        when(produitService.rechercher(null, null)).thenReturn(List.of());
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/catalogue"))
           .andExpect(status().isOk())
           .andExpect(model().attribute("categories", List.of(categorie)));
    }

    // R6 – Accueil affiche le catalogue
    @Test
    @DisplayName("R6 - Accueil affiche le catalogue")
    void R6_accueil_afficheCatalogue() throws Exception {
        when(produitService.rechercher(null, null)).thenReturn(List.of(produit));
        when(categorieService.listerToutes()).thenReturn(List.of(categorie));

        mvc.perform(get("/"))
           .andExpect(status().isOk())
           .andExpect(view().name("catalogue/liste"));
    }
}