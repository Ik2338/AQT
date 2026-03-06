package com.ecommerce.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.PanierRepository;
import com.ecommerce.repository.ProduitRepository;

/**
 * Tests unitaires – PanierService
 *
 * R1 – obtenirOuCreer : retourne le panier existant si present
 * R2 – obtenirOuCreer : cree un nouveau panier si absent
 * R3 – ajouterProduit : ajoute une nouvelle ligne au panier
 * R4 – ajouterProduit : incremente la quantite si produit deja present
 * R5 – ajouterProduit : leve StockInsuffisantException si stock insuffisant
 * R6 – ajouterProduit : leve exception si produit introuvable
 * R7 – modifierQuantite : met a jour la quantite de la ligne
 * R8 – modifierQuantite : supprime la ligne si quantite <= 0
 * R9 – supprimerLigne : retire la ligne du panier
 * R10 – vider : vide toutes les lignes du panier
 */
@ExtendWith(MockitoExtension.class)
class PanierServiceTest {

    @Mock private PanierRepository panierRepo;
    @Mock private ProduitRepository produitRepo;
    @InjectMocks private PanierService panierService;

    private Utilisateur utilisateur;
    private Produit produit;
    private Panier panier;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean@email.com", "pass");
        utilisateur.setId(1L);

        produit = new Produit("Laptop", "Desc", 999.0, 10, null);
        produit.setId(1L);

        panier = new Panier(utilisateur);
        panier.setId(1L);
    }

    // R1 – obtenirOuCreer retourne le panier existant
    @Test
    @DisplayName("R1 - obtenirOuCreer retourne le panier existant")
    void R1_obtenirOuCreer_retournePanierExistant() {
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));

        Panier result = panierService.obtenirOuCreer(utilisateur);

        assertThat(result).isEqualTo(panier);
        verify(panierRepo, never()).save(any());
    }

    // R2 – obtenirOuCreer cree un nouveau panier si absent
    @Test
    @DisplayName("R2 - obtenirOuCreer cree un nouveau panier si absent")
    void R2_obtenirOuCreer_creePanierSiAbsent() {
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.empty());
        when(panierRepo.save(any(Panier.class))).thenReturn(panier);

        Panier result = panierService.obtenirOuCreer(utilisateur);

        assertThat(result).isNotNull();
        verify(panierRepo).save(any(Panier.class));
    }

    // R3 – ajouterProduit ajoute une nouvelle ligne
    @Test
    @DisplayName("R3 - ajouterProduit ajoute une nouvelle ligne au panier")
    void R3_ajouterProduit_ajouteNouvelleLigne() {
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(produitRepo.findById(1L)).thenReturn(Optional.of(produit));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.ajouterProduit(utilisateur, 1L, 2);

        assertThat(panier.getLignes()).hasSize(1);
        assertThat(panier.getLignes().get(0).getQuantite()).isEqualTo(2);
        verify(panierRepo).save(panier);
    }

    // R4 – ajouterProduit incremente la quantite si produit deja present
    @Test
    @DisplayName("R4 - ajouterProduit incremente la quantite si produit deja present")
    void R4_ajouterProduit_incrementeQuantiteSiDejaPresent() {
        LignePanier ligneExistante = new LignePanier(panier, produit, 3);
        ligneExistante.setId(1L);
        panier.getLignes().add(ligneExistante);

        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(produitRepo.findById(1L)).thenReturn(Optional.of(produit));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.ajouterProduit(utilisateur, 1L, 2);

        assertThat(panier.getLignes()).hasSize(1);
        assertThat(panier.getLignes().get(0).getQuantite()).isEqualTo(5);
    }

    // R5 – ajouterProduit leve StockInsuffisantException si stock insuffisant
    @Test
    @DisplayName("R5 - ajouterProduit leve StockInsuffisantException si stock insuffisant")
    void R5_ajouterProduit_leveExceptionSiStockInsuffisant() {
        produit.setStock(2);
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(produitRepo.findById(1L)).thenReturn(Optional.of(produit));

        assertThatThrownBy(() -> panierService.ajouterProduit(utilisateur, 1L, 5))
                .isInstanceOf(StockInsuffisantException.class);

        verify(panierRepo, never()).save(any());
    }

    // R6 – ajouterProduit leve exception si produit introuvable
    @Test
    @DisplayName("R6 - ajouterProduit leve exception si produit introuvable")
    void R6_ajouterProduit_leveExceptionSiProduitIntrouvable() {
        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(produitRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> panierService.ajouterProduit(utilisateur, 99L, 1))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    // R7 – modifierQuantite met a jour la quantite
    @Test
    @DisplayName("R7 - modifierQuantite met a jour la quantite de la ligne")
    void R7_modifierQuantite_metAJourQuantite() {
        LignePanier ligne = new LignePanier(panier, produit, 2);
        ligne.setId(10L);
        panier.getLignes().add(ligne);

        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.modifierQuantite(utilisateur, 10L, 5);

        assertThat(ligne.getQuantite()).isEqualTo(5);
        verify(panierRepo).save(panier);
    }

    // R8 – modifierQuantite supprime la ligne si quantite <= 0
    @Test
    @DisplayName("R8 - modifierQuantite supprime la ligne si quantite <= 0")
    void R8_modifierQuantite_supprimeLigneSiQuantiteZero() {
        LignePanier ligne = new LignePanier(panier, produit, 2);
        ligne.setId(10L);
        panier.getLignes().add(ligne);

        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.modifierQuantite(utilisateur, 10L, 0);

        assertThat(panier.getLignes()).isEmpty();
    }

    // R9 – supprimerLigne retire la ligne du panier
    @Test
    @DisplayName("R9 - supprimerLigne retire la ligne du panier")
    void R9_supprimerLigne_retireLigneDuPanier() {
        LignePanier ligne = new LignePanier(panier, produit, 2);
        ligne.setId(10L);
        panier.getLignes().add(ligne);

        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.supprimerLigne(utilisateur, 10L);

        assertThat(panier.getLignes()).isEmpty();
        verify(panierRepo).save(panier);
    }

    // R10 – vider vide toutes les lignes
    @Test
    @DisplayName("R10 - vider vide toutes les lignes du panier")
    void R10_vider_videToutesLesLignes() {
        panier.getLignes().add(new LignePanier(panier, produit, 1));
        panier.getLignes().add(new LignePanier(panier, produit, 2));

        when(panierRepo.findByUtilisateurId(1L)).thenReturn(Optional.of(panier));
        when(panierRepo.save(any())).thenReturn(panier);

        panierService.vider(utilisateur);

        assertThat(panier.getLignes()).isEmpty();
        verify(panierRepo).save(panier);
    }
}