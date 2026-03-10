package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import com.ecommerce.model.Produit;
import com.ecommerce.repository.ProduitRepository;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock ProduitRepository repo;
    @InjectMocks ProduitService service;

    private Produit produit;

    @BeforeEach
    void setUp() {
        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setActif(true);
    }

    // ─────────────────────────────────────────────────────────
    // R1 – listerTous retourne uniquement les produits actifs
    // CORRECTION : était @DisplayName("R4") avec méthode sans préfixe R_
    // ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("R1 - listerTous retourne uniquement les produits actifs")
    void R1_listerTous_retourneProduitsActifsUniquement() {
        when(repo.findByActifTrue()).thenReturn(List.of(produit));

        assertThat(service.listerTous()).hasSize(1);
        verify(repo).findByActifTrue();
    }

    // ─────────────────────────────────────────────────────────
    // R2 – supprimer désactive le produit (soft delete)
    // CORRECTION : méthode renommée avec préfixe R2_
    // ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("R2 - supprimer desactive le produit (soft delete)")
    void R2_supprimer_desactiveProduit() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));

        service.supprimer(1L);

        assertThat(produit.isActif()).isFalse();
        verify(repo).save(produit);
    }

    // ─────────────────────────────────────────────────────────
    // R3 – trouverParId lève exception si introuvable
    // CORRECTION : méthode renommée avec préfixe R3_
    // ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("R3 - trouverParId leve ResourceNotFoundException si introuvable")
    void R3_trouverParId_leveExceptionSiIntrouvable() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─────────────────────────────────────────────────────────
    // R4 – creer sauvegarde le produit avec actif=true
    // CORRECTION : méthode renommée avec préfixe R4_
    // ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("R4 - creer sauvegarde le produit avec actif=true")
    void R4_creer_sauvegardeAvecActifTrue() {
        Produit p = new Produit("Test", "D", 10.0, 5, null);
        when(repo.save(any())).thenReturn(p);

        Produit result = service.creer(p);

        assertThat(p.isActif()).isTrue();
        verify(repo).save(p);
    }

    // ─────────────────────────────────────────────────────────
    // R5 – mettreAJourStock met à jour le stock
    // CORRECTION : méthode renommée avec préfixe R5_
    // ─────────────────────────────────────────────────────────
    @Test
    @DisplayName("R5 - mettreAJourStock met a jour le stock correctement")
    void R5_mettreAJourStock_metAJourLeStock() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));

        service.mettreAJourStock(1L, 42);

        assertThat(produit.getStock()).isEqualTo(42);
        verify(repo).save(produit);
    }
}