package com.ecommerce.controller;

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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.config.SecurityConfigTest;

/**
 * Tests d'intégration – AuthController
 * Spring démarre complet + H2 en mémoire (profil "test")
 *
 * NOTES sur les redirections :
 * - Le controller fait "redirect:/login"  → URL RELATIVE  → MockMvc voit "/login"
 *   MAIS Spring Security (formLogin) fait une redirection ABSOLUE → "http://localhost/login"
 * - Pour R4 : le controller AuthController redirige vers /login (relatif)
 *   → on attend redirectedUrl("/login")  (pas http://localhost/login)
 * - Pour R6 : le POST /login est géré par Spring Security avec
 *   InMemoryUserDetailsManager ({noop}client123) fourni par SecurityConfigTest
 *   → credentials {username=jean.dupont@email.com, password=client123} doivent marcher
 * - Pour R8 : Spring Security redirige vers l'URL absolue → redirectedUrl("http://localhost/login")
 */


@AutoConfigureMockMvc
@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@ActiveProfiles("test")
@Import(SecurityConfigTest.class)
class AuthControllerIT extends BaseIT {
    @Autowired MockMvc mockMvc;

    // R1 – GET /login retourne la vue login
    @Test
    @DisplayName("R1 - GET /login retourne 200 et la vue login")
    void R1_getLogin_retourne200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    // R2 – GET /login?error affiche le message d'erreur
    @Test
    @DisplayName("R2 - GET /login?error affiche le message d'erreur")
    void R2_getLoginAvecError_afficheErreur() throws Exception {
        mockMvc.perform(get("/login").param("error", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeExists("error"));
    }

    // R3 – GET /inscription retourne le formulaire
    @Test
    @DisplayName("R3 - GET /inscription retourne 200 et le formulaire")
    void R3_getInscription_retourneFormulaire() throws Exception {
        mockMvc.perform(get("/inscription"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/inscription"))
                .andExpect(model().attributeExists("utilisateur"));
    }

    // R4 – POST /inscription avec données valides → redirect /login
    // CORRECTION : le controller fait "redirect:/login" (URL relative)
    // → MockMvc reçoit "/login", pas "http://localhost/login"
    @Test
    @DisplayName("R4 - POST /inscription valide redirige vers /login")
    void R4_postInscriptionValide_redirectLogin() throws Exception {
        mockMvc.perform(post("/inscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Dupont")
                        .param("prenom", "Marie")
                        .param("email", "marie.dupont@test.com")
                        .param("motDePasse", "password123"))
                .andExpect(status().is3xxRedirection())
                // Le controller retourne redirect:/login → URL relative → "/login"
                .andExpect(redirectedUrl("/login"));
    }

    // R5 – POST /inscription email déjà existant → redirect /inscription
    @Test
    @DisplayName("R5 - POST /inscription email existant redirige vers /inscription avec erreur")
    void R5_postInscriptionEmailExistant_redirectInscription() throws Exception {
        // jean.dupont@email.com est créé par DataInitializer
        mockMvc.perform(post("/inscription")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nom", "Dupont")
                        .param("prenom", "Jean")
                        .param("email", "jean.dupont@email.com")
                        .param("motDePasse", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/inscription"));
    }

    // R6 – POST /login credentials valides → redirect /catalogue
    // SecurityConfigTest fournit InMemoryUserDetailsManager avec {noop}client123
    // Le POST /login est traité par Spring Security → redirection vers /catalogue
    @Test
    @DisplayName("R6 - POST /login credentials valides redirige vers /catalogue")
    void R6_postLoginValide_redirectCatalogue() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "jean.dupont@email.com")
                        .param("password", "client123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/catalogue"));
    }

    // R7 – POST /login credentials invalides → redirect /login?error
    @Test
    @DisplayName("R7 - POST /login credentials invalides redirige vers /login?error")
    void R7_postLoginInvalide_redirectLoginError() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("username", "inconnu@test.com")
                        .param("password", "mauvaismdp"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"));
    }

    // R8 – Accès /panier sans authentification → redirect /login
    // Spring Security génère une URL absolue pour les redirections de sécurité
    @Test
    @DisplayName("R8 - Accès /panier sans auth redirige vers /login")
    void R8_panierSansAuth_redirectLogin() throws Exception {
        mockMvc.perform(get("/panier"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }
    @Autowired
    UserDetailsService userDetailsService;

    @Test
    void DEBUG_quel_UDS() {
        System.out.println("=== UDS CLASS = " + userDetailsService.getClass().getName());
        try {
            var u = userDetailsService.loadUserByUsername("jean.dupont@email.com");
            System.out.println("=== PASSWORD = " + u.getPassword());
        } catch (Exception e) {
            System.out.println("=== ERREUR loadUser : " + e.getMessage());
        }
    }
}