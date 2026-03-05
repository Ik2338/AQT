package com.ecommerce.controller;

import com.ecommerce.model.Produit;
import com.ecommerce.repository.CategorieRepository;
import com.ecommerce.service.ProduitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CatalogueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ProduitService produitService;
    @MockBean private CategorieRepository categorieRepo;

    @Test
    @DisplayName("GET /catalogue - affiche la liste des produits")
    void catalogue_affiche_liste() throws Exception {
        Produit p = new Produit();
        p.setId(1L);
        p.setNom("Smartphone");
        p.setPrix(599.99);
        p.setStock(10);
        p.setActif(true);

        when(produitService.rechercher(null, null)).thenReturn(List.of(p));
        when(categorieRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalogue"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/liste"))
                .andExpect(model().attributeExists("produits"));
    }

    @Test
    @DisplayName("GET /catalogue - accessible sans connexion")
    void catalogue_accessible_sans_connexion() throws Exception {
        when(produitService.rechercher(null, null)).thenReturn(List.of());
        when(categorieRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/catalogue"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /produit/{id} - affiche le détail")
    void produit_detail_affiche() throws Exception {
        Produit p = new Produit();
        p.setId(1L);
        p.setNom("Smartphone");
        p.setPrix(599.99);
        p.setActif(true);

        when(produitService.trouverParId(1L)).thenReturn(p);
        when(categorieRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/produit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("catalogue/detail"))
                .andExpect(model().attributeExists("produit"));
    }
}