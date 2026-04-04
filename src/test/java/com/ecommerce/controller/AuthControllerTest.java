package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.config.SecurityConfigTest;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

@WebMvcTest(AuthController.class)
@Import(SecurityConfigTest.class)
class AuthControllerTest {

    @Autowired MockMvc mvc;
    @MockBean UtilisateurService utilisateurService;

    // R1 – Page login affichée
    @Test
    @DisplayName("R1 - Page login affichee")
    void R1_login_afficheFormulaire() throws Exception {
        mvc.perform(get("/login"))
           .andExpect(status().isOk())
           .andExpect(view().name("auth/login"));
    }

    // R2 – Login avec erreur → message dans le modèle
    @Test
    @DisplayName("R2 - Login avec erreur affiche message")
    void R2_login_avecErreur_afficheMessage() throws Exception {
        mvc.perform(get("/login").param("error", ""))
           .andExpect(status().isOk())
           .andExpect(view().name("auth/login"))
           .andExpect(model().attributeExists("error"));
    }

    // R3 – Formulaire inscription affiché
    @Test
    @DisplayName("R3 - Formulaire inscription affiche avec objet utilisateur")
    void R3_inscription_afficheFormulaire() throws Exception {
        mvc.perform(get("/inscription"))
           .andExpect(status().isOk())
           .andExpect(view().name("auth/inscription"))
           .andExpect(model().attributeExists("utilisateur"));
    }

    // R4 – Inscription valide → redirect /login
    // CORRECTION : csrf() ajouté — sans lui le POST est rejeté 403 même si csrf disable
    // ne s'applique pas ici car SecurityConfigTest l'a désactivé, MAIS le controller
    // retourne redirect:/login qui est une URL relative → redirectedUrl("/login") est correct
    @Test
    @DisplayName("R4 - Inscription valide redirige vers login")
    void R4_inscription_valide_redirigeLogin() throws Exception {
        Utilisateur u = new Utilisateur("Dupont", "Jean", "jean@test.com", "pass");
        when(utilisateurService.inscrire(any())).thenReturn(u);

        mvc.perform(post("/inscription")
           .with(csrf())
           .param("nom", "Dupont")
           .param("prenom", "Jean")
           .param("email", "jean@test.com")
           .param("motDePasse", "pass"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/login"));

        verify(utilisateurService).inscrire(any());
    }

    // R5 – Email déjà utilisé → redirect /inscription
    @Test
    @DisplayName("R5 - Email deja utilise redirige vers inscription")
    void R5_inscription_emailDejaUtilise_redirige() throws Exception {
        doThrow(new IllegalArgumentException("Email deja utilise"))
            .when(utilisateurService).inscrire(any());

        mvc.perform(post("/inscription")
           .with(csrf())
           .param("nom", "Dupont")
           .param("prenom", "Jean")
           .param("email", "jean@test.com")
           .param("motDePasse", "pass"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/inscription"));
    }

    // R6 – Login accessible sans authentification
    @Test
    @DisplayName("R6 - Login accessible sans authentification")
    void R6_login_accessibleSansAuth() throws Exception {
        mvc.perform(get("/login"))
           .andExpect(status().isOk());
    }
}