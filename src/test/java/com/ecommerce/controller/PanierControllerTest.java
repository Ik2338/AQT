package com.ecommerce.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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

import java.util.List;

@WebMvcTest(PanierController.class)
@Import(SecurityConfigTest.class)
class PanierControllerTest {

    @Autowired MockMvc mvc;

    @MockBean PanierService      panierService;
    @MockBean CommandeService    commandeService;
    @MockBean UtilisateurService utilisateurService;

    private Utilisateur client;
    private Panier      panier;
    private Produit     produit;

    private static final String EMAIL = "jean@test.com";
    private MockHttpSession sessionAuthentifiee;

    @BeforeEach
    void setUp() {
        client = new Utilisateur("Dupont", "Jean", EMAIL, "pass");
        client.setId(1L);
        panier = new Panier(client);
        panier.setId(1L);
        
        // CORRECTION : Créer un vrai produit pour les tests
        produit = new Produit("Laptop Test", "Description test", 999.99, 10, null);
        produit.setId(1L);

        when(utilisateurService.trouverParEmailOptional(EMAIL))
            .thenReturn(Optional.of(client));

        UsernamePasswordAuthenticationToken authToken =
            new UsernamePasswordAuthenticationToken(
                EMAIL, null,
                List.of(new SimpleGrantedAuthority("ROLE_CLIENT"))
            );
        SecurityContext securityContext = new SecurityContextImpl(authToken);
        sessionAuthentifiee = new MockHttpSession();
        sessionAuthentifiee.setAttribute(
            HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            securityContext
        );
    }

    // ... autres tests ...

    // ─── R8 : Checkout panier non vide → 200 (CORRECTION COMPLÈTE) ─────────
    @Test
    @DisplayName("R8 - Checkout panier non vide affiche page checkout")
    void R8_checkout_panierNonVide_afficheCheckout() throws Exception {
        // CORRECTION : Créer une vraie LignePanier avec un vrai produit
        LignePanier ligne = new LignePanier(panier, produit, 2);
        ligne.setId(1L);
        panier.getLignes().add(ligne);
        
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);

        mvc.perform(get("/panier/checkout").session(sessionAuthentifiee))
           .andExpect(status().isOk())
           .andExpect(view().name("panier/checkout"))
           .andExpect(model().attributeExists("panier"));
    }

    // ─── R9 : Valider commande → redirect /commande/{id} ──────────
    @Test
    @DisplayName("R9 - Valider commande redirige vers detail commande")
    void R9_valider_redirige() throws Exception {
        Commande commande = new Commande(client);
        commande.setId(42L);
        
        when(panierService.obtenirOuCreer(client)).thenReturn(panier);
        when(commandeService.validerPanier(any(), any(), any())).thenReturn(commande);

        mvc.perform(post("/panier/valider")
                .session(sessionAuthentifiee)
                .param("adresse", "12 rue de Paris"))
           .andExpect(status().is3xxRedirection())
           .andExpect(redirectedUrl("/commande/42"));
    }
}