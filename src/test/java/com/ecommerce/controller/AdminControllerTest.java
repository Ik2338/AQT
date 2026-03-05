package com.ecommerce.controller;

import com.ecommerce.model.Produit;
import com.ecommerce.repository.CategorieRepository;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.ProduitService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private ProduitService produitService;
    @MockBean private CommandeService commandeService;
    @MockBean private CategorieRepository categorieRepo;

    // ─── Accès Dashboard ────────────────────────────────────

    @Test
    @DisplayName("GET /admin - accès autorisé pour ADMIN")
    @WithMockUser(username = "admin@ecommerce.com", roles = "ADMIN")
    void admin_acces_autorise() throws Exception {
        when(produitService.listerTous()).thenReturn(List.of());
        when(commandeService.toutesLesCommandes()).thenReturn(List.of());
        when(categorieRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /admin - accès interdit pour CLIENT")
    @WithMockUser(username = "client@test.com", roles = "CLIENT")
    void admin_acces_interdit_client() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /admin - redirect login sans connexion")
    void admin_sans_connexion_redirect_login() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection());
    }

    // ─── Produits ───────────────────────────────────────────

    @Test
    @DisplayName("GET /admin/produits/nouveau - formulaire accessible")
    @WithMockUser(roles = "ADMIN")
    void admin_nouveau_produit_form() throws Exception {
        when(categorieRepo.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/produits/nouveau"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/produits/formulaire"));
    }

    @Test
    @DisplayName("POST /admin/produits/sauvegarder - produit créé")
    @WithMockUser(roles = "ADMIN")
    void admin_creer_produit() throws Exception {
        when(produitService.creer(any())).thenReturn(new Produit());

        mockMvc.perform(post("/admin/produits/nouveau")
                .with(csrf())
                .param("nom", "Nouveau Produit")
                .param("prix", "99.99")
                .param("stock", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("POST /admin/produits/supprimer - produit supprimé")
    @WithMockUser(roles = "ADMIN")
    void admin_supprimer_produit() throws Exception {
        doNothing().when(produitService).supprimer(1L);

        mockMvc.perform(post("/admin/produits/supprimer/1")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    // ─── Catégories ─────────────────────────────────────────

    @Test
    @DisplayName("POST /admin/categories/nouveau - catégorie créée")
    @WithMockUser(roles = "ADMIN")
    void admin_creer_categorie() throws Exception {
        when(categorieRepo.save(any())).thenReturn(null);

        mockMvc.perform(post("/admin/categories/nouveau")
                .with(csrf())
                .param("nom", "Nouvelle Catégorie"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }
}