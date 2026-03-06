package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

/**
 * Tests unitaires – ProfilController
 *
 * R1 – afficherProfil : retourne la vue avec l'utilisateur connecté
 * R2 – modifierProfil : met a jour + flash success + redirect /profil
 */
@ExtendWith(MockitoExtension.class)
class ProfilControllerTest {

    @Mock private UtilisateurService utilisateurService;
    @Mock private Authentication authentication;
    @InjectMocks private ProfilController profilController;

    private Utilisateur utilisateur;
    private Model model;
    private RedirectAttributesModelMap ra;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass");
        utilisateur.setId(1L);
        model = new ConcurrentModel();
        ra = new RedirectAttributesModelMap();
        when(authentication.getName()).thenReturn("jean@email.com");
        when(utilisateurService.trouverParEmail("jean@email.com")).thenReturn(utilisateur);
    }

    // R1 – afficherProfil retourne la vue avec l'utilisateur
    @Test
    void R1_afficherProfil_retourneVueAvecUtilisateur() {
        String vue = profilController.profil(authentication, model);

        assertThat(vue).isEqualTo("profil/profil");
        assertThat(model.getAttribute("utilisateur")).isEqualTo(utilisateur);
    }

    // R2 – modifierProfil met a jour et redirige
    @Test
    void R2_modifierProfil_metAJourEtRedirect() {
        when(utilisateurService.mettreAJourProfil(anyLong(), anyString(), anyString(), any(), any()))
                .thenReturn(utilisateur);

        String vue = profilController.modifier("Martin", "Sophie", "0612345678", "12 rue Paris",
                authentication, ra);

        assertThat(vue).isEqualTo("redirect:/profil");
        assertThat(ra.getFlashAttributes().get("success")).isEqualTo("Profil mis à jour !");
        verify(utilisateurService).mettreAJourProfil(1L, "Martin", "Sophie", "0612345678", "12 rue Paris");
    }
}