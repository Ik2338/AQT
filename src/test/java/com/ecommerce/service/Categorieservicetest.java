package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categorie;
import com.ecommerce.repository.CategorieRepository;

/**
 * Tests unitaires – CategorieService
 *
 * R1 – listerToutes : retourne toutes les categories
 * R2 – trouverParId : retourne la categorie si trouvee
 * R3 – trouverParId : leve ResourceNotFoundException si introuvable
 * R4 – creer : sauvegarde la categorie avec le nom trimé
 * R5 – creer : leve exception si nom null ou vide
 * R6 – supprimer : supprime la categorie existante
 * R7 – supprimer : leve exception si categorie introuvable
 */
@ExtendWith(MockitoExtension.class)
class CategorieServiceTest {

    @Mock private CategorieRepository categorieRepo;
    @InjectMocks private CategorieService categorieService;

    private Categorie categorie;

    @BeforeEach
    void setUp() {
        categorie = new Categorie("Electronique");
        categorie.setId(1L);
    }

    // R1 – listerToutes retourne toutes les categories
    @Test
    @DisplayName("R1 - listerToutes retourne toutes les categories")
    void R1_listerToutes_retourneToutesLesCategories() {
        when(categorieRepo.findAll()).thenReturn(List.of(categorie, new Categorie("Sport")));

        List<Categorie> result = categorieService.listerToutes();

        assertThat(result).hasSize(2);
        verify(categorieRepo).findAll();
    }

    // R2 – trouverParId retourne la categorie
    @Test
    @DisplayName("R2 - trouverParId retourne la categorie si trouvee")
    void R2_trouverParId_retourneCategorie() {
        when(categorieRepo.findById(1L)).thenReturn(Optional.of(categorie));

        Categorie result = categorieService.trouverParId(1L);

        assertThat(result).isEqualTo(categorie);
        assertThat(result.getNom()).isEqualTo("Electronique");
    }

    // R3 – trouverParId leve ResourceNotFoundException si introuvable
    @Test
    @DisplayName("R3 - trouverParId leve ResourceNotFoundException si introuvable")
    void R3_trouverParId_leveExceptionSiIntrouvable() {
        when(categorieRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categorieService.trouverParId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // R4 – creer sauvegarde la categorie avec nom trimé
    @Test
    @DisplayName("R4 - creer sauvegarde la categorie avec le nom trimé")
    void R4_creer_sauvegardeCategorie() {
        when(categorieRepo.save(any())).thenReturn(categorie);

        Categorie result = categorieService.creer("  Electronique  ");

        assertThat(result).isNotNull();
        verify(categorieRepo).save(any(Categorie.class));
    }

    // R5 – creer leve exception si nom vide
    @Test
    @DisplayName("R5 - creer leve exception si nom null ou vide")
    void R5_creer_leveExceptionSiNomVide() {
        assertThatThrownBy(() -> categorieService.creer(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("obligatoire");

        assertThatThrownBy(() -> categorieService.creer(null))
                .isInstanceOf(IllegalArgumentException.class);

        verify(categorieRepo, never()).save(any());
    }

    // R6 – supprimer supprime la categorie existante
    @Test
    @DisplayName("R6 - supprimer supprime la categorie existante")
    void R6_supprimer_supprimeCategorie() {
        when(categorieRepo.existsById(1L)).thenReturn(true);
        doNothing().when(categorieRepo).deleteById(1L);

        categorieService.supprimer(1L);

        verify(categorieRepo).deleteById(1L);
    }

    // R7 – supprimer leve exception si categorie introuvable
    @Test
    @DisplayName("R7 - supprimer leve ResourceNotFoundException si introuvable")
    void R7_supprimer_leveExceptionSiIntrouvable() {
        when(categorieRepo.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> categorieService.supprimer(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categorieRepo, never()).deleteById(any());
    }
}