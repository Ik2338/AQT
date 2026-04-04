package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;

import com.ecommerce.config.SecurityConfigTest;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

@WebMvcTest(ProfilController.class)
@Import(SecurityConfigTest.class)
class ProfilControllerTest {

    @Autowired MockMvc mvc;
    @MockBean UtilisateurService utilisateurService;

    private Utilisateur client;
    private MockHttpSession sessionAuthentifiee;

    private static final String EMAIL = "jean.dupont@email.com";

    @BeforeEach
    void setUp() {
        client = new Utilisateur("Dupont", "Jean", EMAIL, "pass");
        client.setId(1L);
        client.setTelephone("0612345678");
        client.setAdresse("12 rue de Paris");

        when(utilisateurService.trouverParEmailOptional(EMAIL))
            .thenReturn(Optional.of(client));

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
        SecurityContext ctx = new SecurityContextImpl(authToken);
        sessionAuthentifiee = new MockHttpSession();
        sessionAuthentifiee.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
    }

    // R1 – Profil affiché pour CLIENT connecté
    @Test
    @DisplayName("R1 - Profil affiche pour CLIENT connecte")
    void R1_profil_afficheClient() throws Exception {
        mvc.perform(get("/profil").session(sessionAuthentifiee))
           .andExpect(status().isOk())
           .andExpect(view().name("profil/profil"))
           .andExpect(model().attributeExists("utilisateur"));
    }

    // R2 – Profil refuse accès non connecté
    // CORRECTION : redirectedUrlPattern("**/login") car Spring Security génère
    // une URL absolue (http://localhost/login) — redirectedUrl("/login") échoue
    @Test
    @DisplayName("R2 - Profil refuse acces non connecte")
    void R2_profil_refuseNonConnecte() throws Exception {
        mvc.perform(get("/profil"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    // R3 – Modifier profil redirige vers /profil
    @Test
    @DisplayName("R3 - Modifier profil redirige vers profil")
    void R3_modifierProfil_redirige() throws Exception {
        when(utilisateurService.mettreAJourProfil(anyLong(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(client);

        mvc.perform(post("/profil/modifier")
                .session(sessionAuthentifiee)
                .param("nom", "Martin")
                .param("prenom", "Sophie")
                .param("telephone", "0699999999")
                .param("adresse", "5 avenue Victor Hugo"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/profil"));
    }

    // R4 – Service appelé avec les bons paramètres
    @Test
    @DisplayName("R4 - Modifier profil appelle le service avec les bons parametres")
    void R4_modifierProfil_appelleService() throws Exception {
        when(utilisateurService.mettreAJourProfil(anyLong(), anyString(), anyString(),
                anyString(), anyString())).thenReturn(client);

        mvc.perform(post("/profil/modifier")
                .session(sessionAuthentifiee)
                .param("nom", "Martin")
                .param("prenom", "Sophie")
                .param("telephone", "0699999999")
                .param("adresse", "5 avenue Victor Hugo"))
           .andExpect(status().is3xxRedirection());

        verify(utilisateurService).mettreAJourProfil(
            1L, "Martin", "Sophie", "0699999999", "5 avenue Victor Hugo");
    }

    // R5 – Modèle contient l'utilisateur
    @Test
    @DisplayName("R5 - Modele contient l utilisateur connecte")
    void R5_profil_modeleContientUtilisateur() throws Exception {
        mvc.perform(get("/profil").session(sessionAuthentifiee))
           .andExpect(model().attribute("utilisateur", client));
    }
}