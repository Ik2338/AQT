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
 * Tests d'intégration – ProfilController
 *
 * R1 – GET /profil sans auth           → redirect /login
 * R2 – GET /profil avec CLIENT         → 200 + vue profil + utilisateur
 * R3 – POST /profil/modifier valide    → redirect /profil + success
 * R4 – GET /profil avec ADMIN seul     → 403
 */
class ProfilControllerIT extends BaseIT {

    @Autowired MockMvc mockMvc;

    // R1 – GET /profil sans auth → redirect /login
    @Test
    @DisplayName("R1 - GET /profil sans auth redirige vers /login")
    void R1_profilSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/profil"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // R2 – GET /profil avec CLIENT → 200 + vue profil
    @Test
    @DisplayName("R2 - GET /profil avec CLIENT retourne 200 et vue profil")
    void R2_profilAvecClient_retourne200() throws Exception {
        mockMvc.perform(get("/profil")
                        .with(user("jean.dupont@email.com").roles("CLIENT")))
                .andExpect(status().isOk())
                .andExpect(view().name("profil/profil"))
                .andExpect(model().attributeExists("utilisateur"));
    }

    // R3 – POST /profil/modifier avec données valides → redirect /profil
    @Test
    @DisplayName("R3 - POST /profil/modifier valide redirige vers /profil")
    void R3_modifierProfilValide_redirectProfil() throws Exception {
        mockMvc.perform(post("/profil/modifier")
                        .with(user("jean.dupont@email.com").roles("CLIENT"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "DupontModifie")
                        .param("prenom", "JeanModifie")
                        .param("telephone", "0600000000")
                        .param("adresse", "12 Rue de la Paix, Casablanca"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profil"));
    }

    // R4 – GET /profil avec ADMIN seul → 403
    @Test
    @DisplayName("R4 - GET /profil avec role ADMIN seul retourne 403")
    @WithMockUser(username = "admin@ecommerce.com", roles = {"ADMIN"})
    void R4_profilAvecAdminSeul_retourne403() throws Exception {
        mockMvc.perform(get("/profil"))
                .andExpect(status().isForbidden());
    }
}
