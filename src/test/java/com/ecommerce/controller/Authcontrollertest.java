package com.ecommerce.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ConcurrentModel;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import com.ecommerce.model.Utilisateur;
import com.ecommerce.service.UtilisateurService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private UtilisateurService utilisateurService;

    @InjectMocks
    private AuthController authController;

    private Model model;
    private RedirectAttributesModelMap redirectAttributes;

    @BeforeEach
    void setUp() {
        model = new ConcurrentModel();
        redirectAttributes = new RedirectAttributesModelMap();
    }

    // R1 – GET /login sans erreur
    @Test
    void R1_getLogin_retourneVueLoginSansErreur() {
        String vue = authController.login(null, model);
        assertThat(vue).isEqualTo("auth/login");
        assertThat(model.containsAttribute("error")).isFalse();
    }

    // R2 – GET /login?error affiche message erreur
    @Test
    void R2_getLoginAvecParamError_afficheMessageErreur() {
        String vue = authController.login("true", model);
        assertThat(vue).isEqualTo("auth/login");
        assertThat(model.getAttribute("error")).isEqualTo("Email ou mot de passe incorrect.");
    }

    // R3 – GET /inscription retourne formulaire avec utilisateur
    @Test
    void R3_getInscription_retourneFormulaireAvecUtilisateur() {
        String vue = authController.inscriptionForm(model);
        assertThat(vue).isEqualTo("auth/inscription");
        assertThat(model.containsAttribute("utilisateur")).isTrue();
        assertThat(model.getAttribute("utilisateur")).isInstanceOf(Utilisateur.class);
    }

    // R4 – POST /inscription valide : redirect /login + flash success
    @Test
    void R4_postInscriptionValide_redirectLoginAvecSucces() {
        Utilisateur u = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass123");
        when(utilisateurService.inscrire(any(Utilisateur.class))).thenReturn(u);

        String vue = authController.inscrire(u, redirectAttributes);

        assertThat(vue).isEqualTo("redirect:/login");
        assertThat(redirectAttributes.getFlashAttributes().get("success"))
                .isEqualTo("Compte créé ! Connectez-vous.");
        verify(utilisateurService).inscrire(u);
    }

    // R5 – POST /inscription email existant : redirect /inscription + flash error
    @Test
    void R5_postInscriptionEmailExistant_redirectInscriptionAvecErreur() {
        Utilisateur u = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass123");
        doThrow(new RuntimeException("Cet email est déjà utilisé."))
                .when(utilisateurService).inscrire(any(Utilisateur.class));

        String vue = authController.inscrire(u, redirectAttributes);

        assertThat(vue).isEqualTo("redirect:/inscription");
        assertThat(redirectAttributes.getFlashAttributes().get("error"))
                .isEqualTo("Cet email est déjà utilisé.");
    }
}