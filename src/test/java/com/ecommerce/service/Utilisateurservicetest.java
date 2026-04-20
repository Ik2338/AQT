package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import static org.mockito.Mockito.lenient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Role;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceTest {

    @Mock private UtilisateurRepository utilisateurRepo;
    @Mock private RoleRepository        roleRepo;
    @Mock private PasswordEncoder       passwordEncoder;
    @Mock private ApplicationContext    applicationContext;

    private UtilisateurService service;
    private Utilisateur        utilisateur;

    @BeforeEach
    void setUp() {
        service = new UtilisateurService(
                utilisateurRepo, roleRepo, passwordEncoder, applicationContext);

        lenient().when(applicationContext.getBean(UtilisateurService.class))
                 .thenReturn(service);

        utilisateur = new Utilisateur("Dupont", "Jean", "jean@test.com", "pass");
        utilisateur.setId(1L);
        utilisateur.setActif(true);
    }

    // R1 – inscrire crée un utilisateur avec mot de passe encodé et rôle CLIENT
    @Test
    @DisplayName("R1 - inscrire encode le mot de passe et assigne ROLE_CLIENT")
    void R1_inscrire_encodeMotDePasseEtAssigneRole() {
        Utilisateur nouveau = new Utilisateur("Martin", "Alice", "alice@test.com", "secret");

        when(utilisateurRepo.existsByEmail("alice@test.com")).thenReturn(false);
        when(passwordEncoder.encode("secret")).thenReturn("{bcrypt}encoded");
        Role roleClient = new Role("ROLE_CLIENT");
        when(roleRepo.findByNom("ROLE_CLIENT")).thenReturn(Optional.of(roleClient));
        when(utilisateurRepo.save(any())).thenReturn(nouveau);

        Utilisateur result = service.inscrire(nouveau);

        assertThat(result.getMotDePasse()).isEqualTo("{bcrypt}encoded");
        assertThat(result.isActif()).isTrue();
        assertThat(result.getRoles()).contains(roleClient);
        verify(utilisateurRepo).save(nouveau);
    }

    // R2 – inscrire lève une exception si email déjà utilisé
    @Test
    @DisplayName("R2 - inscrire leve IllegalArgumentException si email existant")
    void R2_inscrire_emailExistant_leveException() {
        Utilisateur nouveau = new Utilisateur("Dupont", "Jean", "jean@test.com", "pass");
        when(utilisateurRepo.existsByEmail("jean@test.com")).thenReturn(true);

        assertThatThrownBy(() -> service.inscrire(nouveau))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email deja utilise");
    }

    // R3 – trouverParId retourne l'utilisateur existant
    @Test
    @DisplayName("R3 - trouverParId retourne l utilisateur existant")
    void R3_trouverParId_retourneUtilisateur() {
        when(utilisateurRepo.findById(1L)).thenReturn(Optional.of(utilisateur));

        Utilisateur result = service.trouverParId(1L);

        assertThat(result).isEqualTo(utilisateur);
    }

    // R4 – trouverParId lève ResourceNotFoundException si absent
    @Test
    @DisplayName("R4 - trouverParId leve ResourceNotFoundException si absent")
    void R4_trouverParId_absent_leveException() {
        when(utilisateurRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // R5 – trouverParEmail retourne l'utilisateur si trouvé
    @Test
    @DisplayName("R5 - trouverParEmail retourne l utilisateur si trouve")
    void R5_trouverParEmail_retourneUtilisateur() {
        when(utilisateurRepo.findByEmail("jean@test.com")).thenReturn(Optional.of(utilisateur));

        Utilisateur result = service.trouverParEmail("jean@test.com");

        assertThat(result).isEqualTo(utilisateur);
    }

    // R6 – trouverParEmail lève RuntimeException si absent
    @Test
    @DisplayName("R6 - trouverParEmail leve RuntimeException si absent")
    void R6_trouverParEmail_absent_leveException() {
        when(utilisateurRepo.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParEmail("inconnu@test.com"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // R7 – trouverParEmailOptional retourne Optional présent
    @Test
    @DisplayName("R7 - trouverParEmailOptional retourne Optional present")
    void R7_trouverParEmailOptional_present() {
        when(utilisateurRepo.findByEmail("jean@test.com")).thenReturn(Optional.of(utilisateur));

        Optional<Utilisateur> result = service.trouverParEmailOptional("jean@test.com");

        assertThat(result).isPresent().contains(utilisateur);
    }

    // R8 – trouverParEmailOptional retourne Optional vide si absent
    @Test
    @DisplayName("R8 - trouverParEmailOptional retourne Optional vide si absent")
    void R8_trouverParEmailOptional_absent() {
        when(utilisateurRepo.findByEmail("inconnu@test.com")).thenReturn(Optional.empty());

        Optional<Utilisateur> result = service.trouverParEmailOptional("inconnu@test.com");

        assertThat(result).isEmpty();
    }

    // R9 – mettreAJourProfil met à jour nom, prénom, téléphone, adresse
    @Test
    @DisplayName("R9 - mettreAJourProfil met a jour les champs de profil")
    void R9_mettreAJourProfil_metAJourChamps() {
        when(utilisateurRepo.findById(1L)).thenReturn(Optional.of(utilisateur));
        when(utilisateurRepo.save(any())).thenReturn(utilisateur);

        Utilisateur result = service.mettreAJourProfil(
                1L, "Martin", "Pierre", "0600000000", "1 Rue de la Paix");

        assertThat(result.getNom()).isEqualTo("Martin");
        assertThat(result.getPrenom()).isEqualTo("Pierre");
        assertThat(result.getTelephone()).isEqualTo("0600000000");
        assertThat(result.getAdresse()).isEqualTo("1 Rue de la Paix");
        verify(utilisateurRepo).save(utilisateur);
    }

    // R10 – inscrire sans rôle trouvé dans la base — aucune exception levée
    @Test
    @DisplayName("R10 - inscrire sans role ROLE_CLIENT en base n leve pas d exception")
    void R10_inscrire_sansRoleEnBase_pasException() {
        Utilisateur nouveau = new Utilisateur("X", "Y", "xy@test.com", "pass");
        when(utilisateurRepo.existsByEmail("xy@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("{bcrypt}xyz");
        when(roleRepo.findByNom("ROLE_CLIENT")).thenReturn(Optional.empty());
        when(utilisateurRepo.save(any())).thenReturn(nouveau);

        Utilisateur result = service.inscrire(nouveau);

        assertThat(result).isNotNull();
        assertThat(result.getRoles()).isEmpty();
    }
}