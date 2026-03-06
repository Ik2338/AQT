package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.ecommerce.model.Commande;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.PanierService;
import com.ecommerce.service.UtilisateurService;

/**
 * Tests unitaires – PanierController
 *
 * R1 – voirPanier : retourne la vue panier avec le panier de l'utilisateur
 * R2 – ajouter produit valide : flash success + redirect /panier
 * R3 – ajouter produit invalide (exception) : flash error + redirect /panier
 * R4 – modifier quantite : redirect /panier
 * R5 – supprimer ligne : redirect /panier
 * R6 – checkout panier vide : redirect /panier
 * R7 – checkout panier non vide : retourne vue checkout
 * R8 – valider commande valide : flash success + redirect /commande/{id}
 * R9 – valider commande echoue (exception) : flash error + redirect /panier/checkout
 */
@ExtendWith(MockitoExtension.class)
class PanierControllerTest {

    @Mock private PanierService panierService;
    @Mock private CommandeService commandeService;
    @Mock private UtilisateurService utilisateurService;
    @Mock private Authentication authentication;
    @InjectMocks private PanierController panierController;

    private Utilisateur utilisateur;
    private Panier panier;
    private Model model;
    private RedirectAttributesModelMap ra;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass");
        utilisateur.setId(1L);
        panier = new Panier(utilisateur);
        model = new ConcurrentModel();
        ra = new RedirectAttributesModelMap();
        when(authentication.getName()).thenReturn("jean@email.com");
        when(utilisateurService.trouverParEmail("jean@email.com")).thenReturn(utilisateur);
    }

    // R1 – voirPanier retourne la vue avec le panier
    @Test
    void R1_voirPanier_retourneVuePanier() {
        when(panierService.obtenirOuCreer(utilisateur)).thenReturn(panier);

        String vue = panierController.voirPanier(authentication, model);

        assertThat(vue).isEqualTo("panier/panier");
        assertThat(model.getAttribute("panier")).isEqualTo(panier);
    }

    // R2 – ajouter produit valide : flash success
    @Test
    void R2_ajouterProduitValide_flashSuccessEtRedirect() {
        when(panierService.ajouterProduit(any(), anyLong(), anyInt())).thenReturn(panier);

        String vue = panierController.ajouter(1L, 2, authentication, ra);

        assertThat(vue).isEqualTo("redirect:/panier");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Produit ajouté au panier !");
    }

    // R3 – ajouter produit invalide (stock insuffisant) : flash error
    @Test
    void R3_ajouterProduitInvalide_flashErrorEtRedirect() {
        doThrow(new RuntimeException("Stock insuffisant"))
                .when(panierService).ajouterProduit(any(), anyLong(), anyInt());

        String vue = panierController.ajouter(1L, 99, authentication, ra);

        assertThat(vue).isEqualTo("redirect:/panier");
        assertThat(ra.getFlashAttributes().get("error")).isEqualTo("Stock insuffisant");
    }

    // R4 – modifier quantite : redirect /panier
    @Test
    void R4_modifierQuantite_redirectPanier() {
        when(panierService.modifierQuantite(any(), anyLong(), anyInt())).thenReturn(panier);

        String vue = panierController.modifier(1L, 3, authentication, ra);

        assertThat(vue).isEqualTo("redirect:/panier");
        verify(panierService).modifierQuantite(utilisateur, 1L, 3);
    }

    // R5 – supprimer ligne : redirect /panier
    @Test
    void R5_supprimerLigne_redirectPanier() {
        when(panierService.supprimerLigne(any(), anyLong())).thenReturn(panier);

        String vue = panierController.supprimer(1L, authentication);

        assertThat(vue).isEqualTo("redirect:/panier");
        verify(panierService).supprimerLigne(utilisateur, 1L);
    }

    // R6 – checkout panier vide : redirect /panier
    @Test
    void R6_checkoutPanierVide_redirectPanier() {
        panier.setLignes(new ArrayList<>());
        when(panierService.obtenirOuCreer(utilisateur)).thenReturn(panier);

        String vue = panierController.checkout(authentication, model);

        assertThat(vue).isEqualTo("redirect:/panier");
    }

    // R7 – checkout panier non vide : retourne vue checkout
    @Test
    void R7_checkoutPanierNonVide_retourneVueCheckout() {
        Produit p = new Produit("Laptop", "Desc", 999.0, 5, null);
        LignePanier ligne = new LignePanier(panier, p, 1);
        panier.getLignes().add(ligne);
        when(panierService.obtenirOuCreer(utilisateur)).thenReturn(panier);

        String vue = panierController.checkout(authentication, model);

        assertThat(vue).isEqualTo("panier/checkout");
        assertThat(model.getAttribute("panier")).isEqualTo(panier);
        assertThat(model.getAttribute("utilisateur")).isEqualTo(utilisateur);
    }

    // R8 – valider commande valide : redirect /commande/{id}
    @Test
    void R8_validerCommandeValide_redirectCommande() {
        Commande commande = new Commande(utilisateur);
        commande.setId(42L);
        when(panierService.obtenirOuCreer(utilisateur)).thenReturn(panier);
        when(commandeService.validerPanier(any(), any(), anyString())).thenReturn(commande);

        String vue = panierController.valider("12 rue de Paris", authentication, ra);

        assertThat(vue).isEqualTo("redirect:/commande/42");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Commande #42 confirmée !");
    }

    // R9 – valider commande echoue : flash error + redirect /panier/checkout
    @Test
    void R9_validerCommandeEchoue_flashErrorEtRedirectCheckout() {
        when(panierService.obtenirOuCreer(utilisateur)).thenReturn(panier);
        when(commandeService.validerPanier(any(), any(), any()))
                .thenThrow(new RuntimeException("Panier vide"));

        String vue = panierController.valider(null, authentication, ra);

        assertThat(vue).isEqualTo("redirect:/panier/checkout");
        assertThat(ra.getFlashAttributes().get("error")).isEqualTo("Panier vide");
    }
}