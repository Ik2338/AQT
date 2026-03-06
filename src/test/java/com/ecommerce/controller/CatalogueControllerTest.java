package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
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

import com.ecommerce.model.Categorie;
import com.ecommerce.model.Produit;
import com.ecommerce.service.CategorieService;
import com.ecommerce.service.ProduitService;

/**
 * Tests unitaires – CatalogueController
 * @ExtendWith(MockitoExtension.class) — PAS @SpringBootTest
 * Spring ne démarre PAS, pas de conflit de mapping
 */
@ExtendWith(MockitoExtension.class)
class CatalogueControllerTest {

    @Mock private ProduitService produitService;
    @Mock private CategorieService categorieService;
    @InjectMocks private CatalogueController catalogueController;

    private Model model;
    private Produit produit;
    private List<Categorie> categories;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setId(1L);
        categories = List.of(new Categorie("Electronique"));
        when(categorieService.listerToutes()).thenReturn(categories);
    }

    @Test
    @DisplayName("R1 - catalogue sans filtre retourne tous les produits actifs")
    void R1_catalogueSansFiltre_retourneTousLesProduits() {
        when(produitService.rechercher(null, null)).thenReturn(List.of(produit));

        String vue = catalogueController.catalogue(null, null, model);

        assertThat(vue).isEqualTo("catalogue/liste");
        assertThat((List<?>) model.getAttribute("produits")).hasSize(1);
        assertThat(model.getAttribute("categories")).isEqualTo(categories);
    }

    @Test
    @DisplayName("R2 - catalogue avec recherche par nom")
    void R2_catalogueAvecRecherche_filtreParNom() {
        when(produitService.rechercher("Laptop", null)).thenReturn(List.of(produit));

        String vue = catalogueController.catalogue("Laptop", null, model);

        assertThat(vue).isEqualTo("catalogue/liste");
        assertThat(model.getAttribute("q")).isEqualTo("Laptop");
        verify(produitService).rechercher("Laptop", null);
    }

    @Test
    @DisplayName("R3 - catalogue avec filtre categorie")
    void R3_catalogueAvecCategorie_filtreParCategorie() {
        when(produitService.rechercher(null, 1L)).thenReturn(List.of(produit));

        String vue = catalogueController.catalogue(null, 1L, model);

        assertThat(vue).isEqualTo("catalogue/liste");
        assertThat(model.getAttribute("categorieId")).isEqualTo(1L);
        verify(produitService).rechercher(null, 1L);
    }

    @Test
    @DisplayName("R4 - detail produit retourne la vue avec le produit")
    void R4_detailProduit_retourneVueAvecProduit() {
        when(produitService.trouverParId(1L)).thenReturn(produit);

        String vue = catalogueController.detail(1L, model);

        assertThat(vue).isEqualTo("catalogue/detail");
        assertThat(model.getAttribute("produit")).isEqualTo(produit);
    }
}