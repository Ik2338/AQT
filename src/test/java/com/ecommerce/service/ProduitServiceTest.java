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

    @Test @DisplayName("R4 - listerTous retourne uniquement les produits actifs")
    void listerTous_retourneActifs() {
        when(repo.findByActifTrue()).thenReturn(List.of(produit));
        assertThat(service.listerTous()).hasSize(1);
        verify(repo).findByActifTrue();
    }

    @Test @DisplayName("R4 - supprimerProduit désactive le produit (soft delete)")
    void supprimer_desactiveProduit() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));
        service.supprimer(1L);
        assertThat(produit.isActif()).isFalse();
        verify(repo).save(produit);
    }

    @Test @DisplayName("trouverParId - lève exception si introuvable")
    void trouverParId_exception() {
        when(repo.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.trouverParId(99L))
            .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test @DisplayName("creer - sauvegarde le produit avec actif=true")
    void creer_sauvegardeAvecActif() {
        Produit p = new Produit("Test", "D", 10.0, 5, null);
        when(repo.save(any())).thenReturn(p);
        Produit result = service.creer(p);
        assertThat(p.isActif()).isTrue();
        verify(repo).save(p);
    }

    @Test @DisplayName("mettreAJourStock - met à jour le stock")
    void mettreAJourStock() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));
        service.mettreAJourStock(1L, 42);
        assertThat(produit.getStock()).isEqualTo(42);
        verify(repo).save(produit);
    }
}
