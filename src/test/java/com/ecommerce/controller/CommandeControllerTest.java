package com.ecommerce.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
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
import com.ecommerce.model.Commande;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.UtilisateurService;

@WebMvcTest(CommandeController.class)
@Import(SecurityConfigTest.class)
class CommandeControllerTest {

    @Autowired MockMvc mvc;
    @MockBean CommandeService   commandeService;
    @MockBean UtilisateurService utilisateurService;

    private Utilisateur client;
    private Commande    commande;
    private MockHttpSession sessionAuthentifiee;

    private static final String EMAIL = "jean.dupont@email.com";

    @BeforeEach
    void setUp() {
        client = new Utilisateur("Dupont", "Jean", EMAIL, "pass");
        client.setId(1L);
        commande = new Commande(client);
        commande.setId(1L);
        commande.setEtat(Commande.EtatCommande.VALIDEE);

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

    // R1 – Historique accessible pour CLIENT connecté
    @Test
    @DisplayName("R1 - Historique accessible CLIENT connecte")
    void R1_historique_accessibleClient() throws Exception {
        when(commandeService.historiqueClient(1L)).thenReturn(List.of(commande));

        mvc.perform(get("/commande/historique").session(sessionAuthentifiee))
           .andExpect(status().isOk())
           .andExpect(view().name("commande/historique"))
           .andExpect(model().attributeExists("commandes"));
    }

    // R2 – Historique refuse accès non connecté
    // CORRECTION : redirectedUrlPattern("**/login") au lieu de redirectedUrl("/login")
    // car Spring Security génère une URL absolue (http://localhost/login) en contexte MockMvc
    @Test
    @DisplayName("R2 - Historique refuse acces non connecte")
    void R2_historique_refuseNonConnecte() throws Exception {
        mvc.perform(get("/commande/historique"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    // R3 – Détail commande affiché
    @Test
    @DisplayName("R3 - Detail commande affiche")
    void R3_detail_afficheCommande() throws Exception {
        when(commandeService.trouverParId(1L)).thenReturn(commande);

        mvc.perform(get("/commande/1").session(sessionAuthentifiee))
           .andExpect(status().isOk())
           .andExpect(view().name("commande/detail"))
           .andExpect(model().attributeExists("commande"));
    }

    // R4 – Modèle contient exactement la liste retournée
    @Test
    @DisplayName("R4 - Modele contient la liste des commandes")
    void R4_historique_modeleContientCommandes() throws Exception {
        when(commandeService.historiqueClient(1L)).thenReturn(List.of(commande));

        mvc.perform(get("/commande/historique").session(sessionAuthentifiee))
           .andExpect(model().attribute("commandes", List.of(commande)));
    }
}