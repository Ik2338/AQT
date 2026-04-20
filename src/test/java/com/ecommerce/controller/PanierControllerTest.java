package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.Commande;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.PanierService;
import com.ecommerce.service.UtilisateurService;

@WebMvcTest(PanierController.class)
@Import(SecurityConfigTest.class)
class PanierControllerTest {

    @Autowired MockMvc mvc;
    @MockBean PanierService      panierService;
    @MockBean CommandeService    commandeService;
    @MockBean UtilisateurService utilisateurService;

    private Utilisateur     client;
    private Panier          panier;
    private Produit         produit;
    private MockHttpSession sessionAuth;

    private static final String EMAIL = "jean@test.com";

    @BeforeEach
    void setUp() {
        client  = new Utilisateur("Dupont", "Jean", EMAIL, "pass");
        client.setId(1L);
        panier  = new Panier(client);
        panier.setId(1L);
        produit = new Produit("Laptop Test", "Description", 999.99, 10, null);
        produit.setId(1L);

        when(utilisateurService.trouverParEmailOptional(EMAIL))
                .thenReturn(Optional.of(client));

        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(
                        EMAIL, null,
                        List.of(new SimpleGrantedAuthority("ROLE_CLIENT")));
        SecurityContext ctx = new SecurityContextImpl(token);
        sessionAuth = new MockHttpSession();
        sessionAuth.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
    }

    // ─── GET /panier ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("R1 - GET /panier sans auth → redirect /login (Spring Security)")
    void R1_voirPanier_sansAuth_redirectLogin() throws Exception {
        mvc.perform(get("/panier"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("R2 - GET /panier authentifié → 200 vue panier/panier")
    void R2_voirPanier_authentifie_retourne200() throws Exception {
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);

        mvc.perform(get("/panier").session(sessionAuth))
           .andExpect(status().isOk())
           .andExpect(view().name("panier/panier"))
           .andExpect(model().attributeExists("panier"));
    }

    // ─── POST /panier/ajouter ────────────────────────────────────────────────

    @Test
    @DisplayName("R3 - POST /panier/ajouter sans auth → redirect /login avec erreur flash")
    void R3_ajouter_sansAuth_redirectLogin() throws Exception {
        mvc.perform(post("/panier/ajouter")
                .with(csrf())
                .param("produitId", "1")
                .param("quantite", "1"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("R4 - POST /panier/ajouter valide → redirect /panier + flash success")
    void R4_ajouter_valide_redirectPanier() throws Exception {
        mvc.perform(post("/panier/ajouter")
                .session(sessionAuth)
                .with(csrf())
                .param("produitId", "1")
                .param("quantite", "2"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier"));

        verify(panierService).ajouterProduit(client, 1L, 2);
    }

    @Test
    @DisplayName("R5 - POST /panier/ajouter stock insuffisant → redirect /panier + flash error")
    void R5_ajouter_stockInsuffisant_redirectPanierAvecErreur() throws Exception {
        doThrow(new StockInsuffisantException("Laptop", 1, 99))
                .when(panierService).ajouterProduit(any(), anyLong(), anyInt());

        mvc.perform(post("/panier/ajouter")
                .session(sessionAuth)
                .with(csrf())
                .param("produitId", "1")
                .param("quantite", "99"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier"));
    }

    // ─── POST /panier/modifier/{ligneId} ─────────────────────────────────────

    @Test
    @DisplayName("R6 - POST /panier/modifier sans auth → redirect /login")
    void R6_modifier_sansAuth_redirectLogin() throws Exception {
        mvc.perform(post("/panier/modifier/1")
                .with(csrf())
                .param("quantite", "3"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));

        verify(panierService, never()).modifierQuantite(any(), anyLong(), anyInt());
    }

    @Test
    @DisplayName("R7 - POST /panier/modifier authentifié → redirect /panier")
    void R7_modifier_authentifie_redirectPanier() throws Exception {
        mvc.perform(post("/panier/modifier/10")
                .session(sessionAuth)
                .with(csrf())
                .param("quantite", "3"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier"));

        verify(panierService).modifierQuantite(client, 10L, 3);
    }

    // ─── POST /panier/supprimer/{ligneId} ────────────────────────────────────

    @Test
    @DisplayName("R8 - POST /panier/supprimer sans auth → redirect /login")
    void R8_supprimer_sansAuth_redirectLogin() throws Exception {
        mvc.perform(post("/panier/supprimer/1").with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));

        verify(panierService, never()).supprimerLigne(any(), anyLong());
    }

    @Test
    @DisplayName("R9 - POST /panier/supprimer authentifié → redirect /panier")
    void R9_supprimer_authentifie_redirectPanier() throws Exception {
        mvc.perform(post("/panier/supprimer/10")
                .session(sessionAuth)
                .with(csrf()))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier"));

        verify(panierService).supprimerLigne(client, 10L);
    }

    // ─── GET /panier/checkout ────────────────────────────────────────────────

    @Test
    @DisplayName("R10 - GET /panier/checkout sans auth → redirect /login")
    void R10_checkout_sansAuth_redirectLogin() throws Exception {
        mvc.perform(get("/panier/checkout"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("R11 - GET /panier/checkout panier vide → redirect /panier")
    void R11_checkout_panierVide_redirectPanier() throws Exception {
        when(panierService.obtenirOuCreer(client)).thenReturn(panier); // lignes vides

        mvc.perform(get("/panier/checkout").session(sessionAuth))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier"));
    }

    @Test
    @DisplayName("R12 - GET /panier/checkout panier non vide → 200 vue checkout")
    void R12_checkout_panierNonVide_afficheCheckout() throws Exception {
        LignePanier ligne = new LignePanier(panier, produit, 2);
        ligne.setId(1L);
        panier.getLignes().add(ligne);
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);

        mvc.perform(get("/panier/checkout").session(sessionAuth))
           .andExpect(status().isOk())
           .andExpect(view().name("panier/checkout"))
           .andExpect(model().attributeExists("panier"))
           .andExpect(model().attributeExists("utilisateur"));
    }

    // ─── POST /panier/valider ────────────────────────────────────────────────

    @Test
    @DisplayName("R13 - POST /panier/valider sans auth → redirect /login")
    void R13_valider_sansAuth_redirectLogin() throws Exception {
        mvc.perform(post("/panier/valider")
                .with(csrf())
                .param("adresse", "12 Rue Test"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("R14 - POST /panier/valider succès → redirect /commande/{id}")
    void R14_valider_succes_redirectCommande() throws Exception {
        Commande commande = new Commande(client);
        commande.setId(42L);
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);
        when(commandeService.validerPanier(any(), any(), any())).thenReturn(commande);

        mvc.perform(post("/panier/valider")
                .session(sessionAuth)
                .with(csrf())
                .param("adresse", "12 Rue de Paris"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/commande/42"));
    }

    @Test
    @DisplayName("R15 - POST /panier/valider exception → redirect /panier/checkout avec erreur")
    void R15_valider_exception_redirectCheckout() throws Exception {
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);
        when(commandeService.validerPanier(any(), any(), any()))
                .thenThrow(new IllegalStateException("Le panier est vide."));

        mvc.perform(post("/panier/valider")
                .session(sessionAuth)
                .with(csrf())
                .param("adresse", "12 Rue Test"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/panier/checkout"));
    }
}