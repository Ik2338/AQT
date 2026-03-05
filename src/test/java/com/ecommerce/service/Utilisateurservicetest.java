package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.model.Role;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

/**
 * Tests unitaires – UtilisateurService
 * Couvre : inscription (R1–R5) et profil (R6)
 *
 * Règles métier testées :
 *  R1 – Inscription valide : compte créé avec rôle ROLE_CLIENT
 *  R2 – Email déjà utilisé : lève une exception
 *  R3 – Mot de passe encodé à l'enregistrement
 *  R4 – Compte actif par défaut après inscription
 *  R5 – trouverParEmail lève exception si email introuvable
 *  R6 – mettreAJourProfil modifie nom, prénom, téléphone et adresse
 */
@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UtilisateurService utilisateurService;

    private Utilisateur utilisateur;
    private Role roleClient;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean.dupont@email.com", "motdepasse123");
        roleClient = new Role("ROLE_CLIENT");
        roleClient.setId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R1 – Inscription valide : compte créé avec rôle ROLE_CLIENT
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R1_inscriptionValide_compteCreéAvecRoleClient() {
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(roleRepository.findByNom("ROLE_CLIENT")).thenReturn(Optional.of(roleClient));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = utilisateurService.inscrire(utilisateur);

        assertThat(result).isNotNull();
        assertThat(result.getRoles()).extracting(Role::getNom).contains("ROLE_CLIENT");
        verify(utilisateurRepository).save(utilisateur);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R2 – Email déjà utilisé : lève une exception
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R2_emailDejaUtilise_leveException() {
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> utilisateurService.inscrire(utilisateur))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");

        verify(utilisateurRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R3 – Mot de passe encodé à l'enregistrement
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R3_motDePasseEncodeLorsInscription() {
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(roleRepository.findByNom("ROLE_CLIENT")).thenReturn(Optional.of(roleClient));
        when(passwordEncoder.encode("motdepasse123")).thenReturn("$2a$encoded_password");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = utilisateurService.inscrire(utilisateur);

        assertThat(result.getMotDePasse()).isEqualTo("$2a$encoded_password");
        verify(passwordEncoder).encode("motdepasse123");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R4 – Compte actif par défaut après inscription
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R4_compteActifParDefautApresInscription() {
        when(utilisateurRepository.existsByEmail(utilisateur.getEmail())).thenReturn(false);
        when(roleRepository.findByNom("ROLE_CLIENT")).thenReturn(Optional.of(roleClient));
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$encoded");
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        Utilisateur result = utilisateurService.inscrire(utilisateur);

        assertThat(result.isActif()).isTrue();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R5 – trouverParEmail lève exception si email introuvable
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R5_trouverParEmail_leveExceptionSiIntrouvable() {
        when(utilisateurRepository.findByEmail("inconnu@email.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> utilisateurService.trouverParEmail("inconnu@email.com"))
                .isInstanceOf(RuntimeException.class);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // R6 – mettreAJourProfil modifie nom, prénom, téléphone et adresse
    // ─────────────────────────────────────────────────────────────────────────
    @Test
    void R6_mettreAJourProfil_modifieLesChampsCorrectement() {
        utilisateur.setId(1L);
        when(utilisateurRepository.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepository.save(any(Utilisateur.class))).thenAnswer(inv -> inv.getArgument(0));

        utilisateurService.mettreAJourProfil(1L, "Martin", "Sophie", "0612345678", "12 rue de Paris");

        assertThat(utilisateur.getNom()).isEqualTo("Martin");
        assertThat(utilisateur.getPrenom()).isEqualTo("Sophie");
        assertThat(utilisateur.getTelephone()).isEqualTo("0612345678");
        assertThat(utilisateur.getAdresse()).isEqualTo("12 rue de Paris");
        verify(utilisateurRepository).save(utilisateur);
    }
}