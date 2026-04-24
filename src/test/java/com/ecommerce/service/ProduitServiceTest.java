package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Produit;
import com.ecommerce.repository.ProduitRepository;

@ExtendWith(MockitoExtension.class)
class ProduitServiceTest {

    @Mock private ProduitRepository repo;
    @Mock private ApplicationContext applicationContext;

    private ProduitService service;

    private Produit produit;

    @BeforeEach
    void setUp() {
        service = new ProduitService(repo, applicationContext);

        lenient().when(applicationContext.getBean(ProduitService.class))
                 .thenReturn(service);

        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setId(1L);
        produit.setActif(true);
    }

    // R1 – listerTous retourne les produits actifs
    @Test
    @DisplayName("R1 - listerTous retourne tous les produits (admin)")
    void R1_listerTous_retourneTousProduits() {
        when(repo.findAll()).thenReturn(List.of(produit));

        List<Produit> result = service.listerTous();

        assertThat(result).hasSize(1).contains(produit);
        verify(repo).findAll(); // ✅ corrigé
    }

    // R2 – trouverParId retourne le produit existant
    @Test
    @DisplayName("R2 - trouverParId retourne le produit existant")
    void R2_trouverParId_retourneProduit() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));

        Produit result = service.trouverParId(1L);

        assertThat(result).isEqualTo(produit);
    }

    // R3 – trouverParId lève ResourceNotFoundException si absent
    @Test
    @DisplayName("R3 - trouverParId leve ResourceNotFoundException si absent")
    void R3_trouverParId_leveExceptionSiAbsent() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.trouverParId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // R4 – creer marque le produit actif et le sauvegarde
    @Test
    @DisplayName("R4 - creer marque actif et sauvegarde")
    void R4_creer_marqueProduitActif() {
        Produit nouveau = new Produit("Souris", "Desc", 29.0, 5, null);
        when(repo.save(any())).thenReturn(nouveau);

        service.creer(nouveau);

        assertThat(nouveau.isActif()).isTrue();
        verify(repo).save(nouveau);
    }

    // R5 – modifier met à jour tous les champs du produit
    @Test
    @DisplayName("R5 - modifier met a jour tous les champs du produit")
    void R5_modifier_metAJourChamps() {
        Produit data = new Produit("Laptop Pro", "Nouvelle desc", 1299.0, 5, null);
        data.setImageUrl("http://img.test/laptop.png");

        when(repo.findById(1L)).thenReturn(Optional.of(produit));
        when(repo.save(any())).thenReturn(produit);

        service.modifier(1L, data);

        assertThat(produit.getNom()).isEqualTo("Laptop Pro");
        assertThat(produit.getDescription()).isEqualTo("Nouvelle desc");
        assertThat(produit.getPrix()).isEqualTo(1299.0);
        assertThat(produit.getStock()).isEqualTo(5);
        assertThat(produit.getImageUrl()).isEqualTo("http://img.test/laptop.png");
        verify(repo).save(produit);
    }

    // R6 – supprimer désactive le produit (soft delete)
    @Test
    @DisplayName("R6 - supprimer desactive le produit")
    void R6_supprimer_desactiveProduit() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));

        service.supprimer(1L);

        assertThat(produit.isActif()).isFalse();
        verify(repo).save(produit);
    }

    // R7 – mettreAJourStock modifie uniquement le stock
    @Test
    @DisplayName("R7 - mettreAJourStock modifie le stock")
    void R7_mettreAJourStock_modifieStock() {
        when(repo.findById(1L)).thenReturn(Optional.of(produit));

        service.mettreAJourStock(1L, 99);

        assertThat(produit.getStock()).isEqualTo(99);
        verify(repo).save(produit);
    }

    // R8 – rechercher avec nom et categorieId
    @Test
    @DisplayName("R8 - rechercher avec nom et categorieId")
    void R8_rechercher_avecNomEtCategorie() {
        when(repo.findByNomContainingIgnoreCaseAndCategorieIdAndActifTrue("lap", 1L))
                .thenReturn(List.of(produit));

        List<Produit> result = service.rechercher("lap", 1L);

        assertThat(result).hasSize(1);
    }

    // R9 – rechercher avec nom seulement
    @Test
    @DisplayName("R9 - rechercher avec nom seulement")
    void R9_rechercher_avecNomSeulement() {
        when(repo.findByNomContainingIgnoreCaseAndActifTrue("lap"))
                .thenReturn(List.of(produit));

        List<Produit> result = service.rechercher("lap", null);

        assertThat(result).hasSize(1);
    }

    // R10 – rechercher avec categorieId seulement
    @Test
    @DisplayName("R10 - rechercher avec categorieId seulement")
    void R10_rechercher_avecCategorieSeulement() {
        when(repo.findByCategorieIdAndActifTrue(1L)).thenReturn(List.of(produit));

        List<Produit> result = service.rechercher(null, 1L);

        assertThat(result).hasSize(1);
    }

    // R11 – rechercher sans filtre retourne tous les produits actifs
    @Test
    @DisplayName("R11 - rechercher sans filtre retourne tous les produits actifs")
    void R11_rechercher_sansFiltre_retourneTous() {
        when(repo.findByActifTrue()).thenReturn(List.of(produit));

        List<Produit> result = service.rechercher(null, null);

        assertThat(result).hasSize(1);
    }

    // R12 – rechercher avec nom blanc traité comme null
    @Test
    @DisplayName("R12 - rechercher avec nom blanc retourne tous les actifs")
    void R12_rechercher_avecNomBlancSansCategorie_retourneTous() {
        when(repo.findByActifTrue()).thenReturn(List.of(produit));

        List<Produit> result = service.rechercher("   ", null);

        assertThat(result).hasSize(1);
    }
}