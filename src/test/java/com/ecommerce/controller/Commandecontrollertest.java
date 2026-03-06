package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;

import com.ecommerce.model.Commande;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.CommandeService;
import com.ecommerce.service.UtilisateurService;

/**
 * Tests unitaires – CommandeController
 *
 * R1 – historique : retourne la vue avec les commandes du client connecté
 * R2 – detail : retourne la vue avec la commande trouvée par ID
 */
@ExtendWith(MockitoExtension.class)
class CommandeControllerTest {

    @Mock private CommandeService commandeService;
    @Mock private UtilisateurService utilisateurService;
    @Mock private Authentication authentication;
    @InjectMocks private CommandeController commandeController;

    private Utilisateur utilisateur;
    private Model model;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass");
        utilisateur.setId(1L);
        model = new ConcurrentModel();
        // stubs déplacés dans chaque test pour éviter UnnecessaryStubbingException
    }

    // R1 – historique retourne la liste des commandes du client
    @Test
    void R1_historique_retourneVueAvecCommandes() {
        when(authentication.getName()).thenReturn("jean@email.com");
        when(utilisateurService.trouverParEmail("jean@email.com")).thenReturn(utilisateur);
        when(commandeService.historiqueClient(1L)).thenReturn(
                List.of(new Commande(utilisateur), new Commande(utilisateur)));

        String vue = commandeController.historique(authentication, model);

        assertThat(vue).isEqualTo("commande/historique");
        assertThat((List<?>) model.getAttribute("commandes")).hasSize(2);
        verify(commandeService).historiqueClient(1L);
    }

    // R2 – detail retourne la commande par ID
    @Test
    void R2_detail_retourneVueAvecCommande() {
        Commande commande = new Commande(utilisateur);
        commande.setId(10L);
        when(commandeService.trouverParId(10L)).thenReturn(commande);

        String vue = commandeController.detail(10L, model);

        assertThat(vue).isEqualTo("commande/detail");
        assertThat(model.getAttribute("commande")).isEqualTo(commande);
    }
}