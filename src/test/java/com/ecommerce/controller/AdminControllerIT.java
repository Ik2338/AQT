package com.ecommerce.controller;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

class AdminControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // ─── SÉCURITÉ ────────────────────────────────────────────────

    @Test
    @DisplayName("R1 - GET /admin sans auth redirige vers /login")
    void R1_adminSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                // Spring renvoie l'URL absolue http://localhost/login en IT
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    @Test
    @DisplayName("R2 - GET /admin avec CLIENT seul retourne 403")
    @WithMockUser(username = "jean.dupont@email.com", roles = {"CLIENT"})
    void R2_adminAvecClientSeul_retourne403() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

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

    @Test
    @DisplayName("R4 - GET /admin/produits retourne 200 et liste des produits")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R4_adminProduits_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/produits"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/produits/liste"))
                .andExpect(model().attributeExists("produits"));
    }

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

    // CORRECTION R7 : on crée d'abord le produit, puis on supprime celui qu'on vient de créer
    // au lieu de supposer que l'ID 1 existe dans data.sql
    @Test
    @DisplayName("R7 - POST /admin/produits/supprimer/{id} desactive et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R7_adminSupprimerProduit_redirectListeProduits() throws Exception {
        // Étape 1 : créer un produit pour avoir un ID valide
        var result = mockMvc.perform(post("/admin/produits/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Produit A Supprimer")
                        .param("prix", "10.00")
                        .param("stock", "5"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Étape 2 : récupérer la liste pour trouver l'ID du produit créé
        var listResult = mockMvc.perform(get("/admin/produits"))
                .andExpect(status().isOk())
                .andReturn();

        // Étape 3 : extraire l'ID depuis le modèle
        var produits = (java.util.List<?>) listResult.getModelAndView().getModel().get("produits");
        assertNotNull(produits, "La liste de produits ne doit pas être nulle");
        assertTrue(!produits.isEmpty(), "Il doit y avoir au moins un produit");

        Long produitId = ((com.ecommerce.model.Produit) produits.get(produits.size() - 1)).getId();

        // Étape 4 : supprimer ce produit
        mockMvc.perform(post("/admin/produits/supprimer/" + produitId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    // ─── CATÉGORIES ──────────────────────────────────────────────

    @Test
    @DisplayName("R8 - GET /admin/categories retourne 200 et la liste")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R8_adminCategories_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/categories"))
                .andExpect(model().attributeExists("categories"));
    }

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

    // CORRECTION R10 : on crée une catégorie puis on supprime celle qu'on vient de créer
    @Test
    @DisplayName("R10 - POST /admin/categories/supprimer/{id} supprime et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R10_adminSupprimerCategorie_redirectListeCategories() throws Exception {
        // Étape 1 : créer une catégorie
        mockMvc.perform(post("/admin/categories/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Categorie A Supprimer"))
                .andExpect(status().is3xxRedirection());

        // Étape 2 : récupérer la liste pour trouver l'ID de la catégorie créée
        var listResult = mockMvc.perform(get("/admin/categories"))
                .andExpect(status().isOk())
                .andReturn();

        var categories = (java.util.List<?>) listResult.getModelAndView().getModel().get("categories");
        assertNotNull(categories, "La liste de catégories ne doit pas être nulle");
        assertTrue(!categories.isEmpty(), "Il doit y avoir au moins une catégorie");

        Long catId = ((com.ecommerce.model.Categorie) categories.get(categories.size() - 1)).getId();

        // Étape 3 : supprimer cette catégorie
        mockMvc.perform(post("/admin/categories/supprimer/" + catId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }

    // ─── COMMANDES ───────────────────────────────────────────────

    @Test
    @DisplayName("R11 - GET /admin/commandes retourne 200 et la liste des commandes")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R11_adminCommandes_retourneListe() throws Exception {
        mockMvc.perform(get("/admin/commandes"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/commandes"))
                .andExpect(model().attributeExists("commandes"));
    }

    // CORRECTION R12 : flux complet auto-suffisant — crée produit + commande sans dépendre de data.sql
    @Test
    @DisplayName("R12 - POST /admin/commandes/{id}/etat change l etat et redirige")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN", "CLIENT"})
    void R12_adminChangerEtatCommande_redirect() throws Exception {
        // Étape 1 : créer un produit pour avoir quelque chose à ajouter au panier
    	mockMvc.perform(post("/admin/produits/nouveau")
    	        .with(csrf())
    	        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    	        .param("nom", "Produit A Supprimer")
    	        .param("prix", "10.00")
    	        .param("stock", "5"))
    	    .andExpect(status().is3xxRedirection());

        // Étape 2 : récupérer l'ID du produit créé
        var listResult = mockMvc.perform(get("/admin/produits")).andReturn();
        var produits = (java.util.List<?>) listResult.getModelAndView().getModel().get("produits");
        Long produitId = ((com.ecommerce.model.Produit) produits.get(produits.size() - 1)).getId();

        // Étape 3 : ajouter au panier
        mockMvc.perform(post("/panier/ajouter")
                        .with(csrf())
                        .param("produitId", String.valueOf(produitId))
                        .param("quantite", "1"));

        // Étape 4 : valider le panier
        var result = mockMvc.perform(post("/panier/valider")
                        .with(csrf())
                        .param("adresse", "12 Rue Test, Casablanca"))
                .andReturn();

        String redirectUrl = result.getResponse().getRedirectedUrl();
        assertNotNull(redirectUrl, "La redirection vers /commande/{id} est attendue");
        assertTrue(redirectUrl.startsWith("/commande/"),
                "L URL doit commencer par /commande/ mais était : " + redirectUrl);

        // Étape 5 : changer l'état
        String commandeId = redirectUrl.replace("/commande/", "");
        mockMvc.perform(post("/admin/commandes/" + commandeId + "/etat")
                        .with(csrf())
                        .param("etat", "EN_COURS"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/commandes"));
    }
 // ─── COUVERTURE DES CONDITIONS MANQUANTES ────────────────────────────────

    @Test
    @DisplayName("R_COV1 - POST /admin/produits/nouveau sans categorieId couvre la branche null")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void RCOV1_creerProduitSansCategorie_brancheNull() throws Exception {
        mockMvc.perform(post("/admin/produits/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Produit Sans Categorie")
                        .param("prix", "19.99")
                        .param("stock", "5"))
                // pas de param "categorieId" → branche null couverte ✅
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R_COV2 - POST /admin/produits/nouveau avec categorieId invalide couvre la branche catch")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void RCOV2_creerProduitAvecCategorieInvalide_brancheCatch() throws Exception {
        mockMvc.perform(post("/admin/produits/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Produit Categorie Invalide")
                        .param("prix", "29.99")
                        .param("stock", "3")
                        .param("categorieId", "99999"))  // ID inexistant → ResourceNotFoundException
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/produits"));
    }

    @Test
    @DisplayName("R_COV3 - POST /admin/categories/nouveau avec nom vide ne crée pas de categorie")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void RCOV3_creerCategorieNomVide_brancheNomBlank() throws Exception {
        mockMvc.perform(post("/admin/categories/nouveau")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "   "))  
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/categories"));
    }
}