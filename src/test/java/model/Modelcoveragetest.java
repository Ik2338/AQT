package model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.ecommerce.model.Commande;
import com.ecommerce.model.LigneCommande;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;

/**
 * Tests unitaires des modèles Panier, LignePanier, LigneCommande.
 */
class ModelCoverageTest {

    private Utilisateur utilisateur;
    private Produit produit;

    private static final double DELTA = 0.01;

    @BeforeEach
    void setUp() {
        utilisateur = new Utilisateur("Dupont", "Jean", "jean@test.com", "pass");
        utilisateur.setId(1L);

        produit = new Produit("Laptop", "Desc", 999.99, 10, null);
        produit.setId(1L);
    }

    // ─── Panier ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Panier - getTotal sur panier vide retourne 0.0")
    void panier_getTotalVide_retourneZero() {
        Panier panier = new Panier(utilisateur);

        assertThat(panier.getTotal())
                .isCloseTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Panier - getTotal avec lignes calcule correctement")
    void panier_getTotalAvecLignes_calculeCorrectement() {
        Panier panier = new Panier(utilisateur);
        panier.getLignes().add(new LignePanier(panier, produit, 3)); // 2999.97

        assertThat(panier.getTotal())
                .isCloseTo(2999.97, within(DELTA));
    }

    @Test
    @DisplayName("Panier - produit null retourne 0.0")
    void panier_getTotalAvecProduitNull_retourneZero() {
        Panier panier = new Panier(utilisateur);
        LignePanier ligne = new LignePanier();
        ligne.setProduit(null);
        ligne.setQuantite(2);

        panier.getLignes().add(ligne);

        assertThat(panier.getTotal())
                .isCloseTo(0.0, within(DELTA));
    }

    @Test
    @DisplayName("Panier - nombre articles somme correctement")
    void panier_getNombreArticles_sommeLesQuantites() {
        Panier panier = new Panier(utilisateur);
        panier.getLignes().add(new LignePanier(panier, produit, 2));
        panier.getLignes().add(new LignePanier(panier, produit, 3));

        assertThat(panier.getNombreArticles()).isEqualTo(5);
    }

    // ─── LignePanier ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("LignePanier - sous total correct")
    void lignePanier_getSousTotal_calculeCorrectement() {
        Panier panier = new Panier(utilisateur);
        LignePanier ligne = new LignePanier(panier, produit, 4); // 3999.96

        assertThat(ligne.getSousTotal())
                .isCloseTo(3999.96, within(DELTA));
    }

    @Test
    @DisplayName("LignePanier - produit null retourne 0")
    void lignePanier_getSousTotalProduitNull_retourneZero() {
        LignePanier ligne = new LignePanier();
        ligne.setProduit(null);
        ligne.setQuantite(3);

        assertThat(ligne.getSousTotal())
                .isCloseTo(0.0, within(DELTA));
    }

    // ─── LigneCommande ───────────────────────────────────────────────────────

    @Test
    @DisplayName("LigneCommande - constructeur capture prix")
    void ligneCommande_constructeur_capturePrixUnitaire() {
        Utilisateur client = new Utilisateur("Test", "User", "t@t.com", "p");
        Commande commande = new Commande(client);

        LigneCommande ligne = new LigneCommande(commande, produit, 2);

        assertThat(ligne.getPrixUnitaire())
                .isCloseTo(999.99, within(DELTA));

        assertThat(ligne.getQuantite()).isEqualTo(2);
        assertThat(ligne.getProduit()).isEqualTo(produit);
        assertThat(ligne.getCommande()).isEqualTo(commande);
    }

    @Test
    @DisplayName("LigneCommande - sous total correct")
    void ligneCommande_getSousTotal_calculeCorrectement() {
        Utilisateur client = new Utilisateur("Test", "User", "t@t.com", "p");
        Commande commande = new Commande(client);

        LigneCommande ligne = new LigneCommande(commande, produit, 3); // 2999.97

        assertThat(ligne.getSousTotal())
                .isCloseTo(2999.97, within(DELTA));
    }

    @Test
    @DisplayName("LigneCommande - setters fonctionnent")
    void ligneCommande_settersGetters() {
        LigneCommande ligne = new LigneCommande();
        ligne.setId(7L);
        ligne.setProduit(produit);
        ligne.setQuantite(5);
        ligne.setPrixUnitaire(199.0);

        assertThat(ligne.getSousTotal())
                .isCloseTo(995.0, within(DELTA));
    }

    // ─── Commande ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Commande - total calculé correctement")
    void commande_recalculerTotal_additionneCorrectement() {
        Utilisateur client = new Utilisateur("A", "B", "a@b.com", "p");
        Commande commande = new Commande(client);

        LigneCommande l1 = new LigneCommande(commande, produit, 2); // 1999.98
        Produit p2 = new Produit("Souris", "D", 29.99, 5, null);
        LigneCommande l2 = new LigneCommande(commande, p2, 1); // 29.99

        commande.getLignes().add(l1);
        commande.getLignes().add(l2);
        commande.recalculerTotal();

        assertThat(commande.getMontantTotal())
                .isCloseTo(2029.97, within(DELTA));
    }

    @Test
    @DisplayName("Commande - setters fonctionnent")
    void commande_settersEtatAdresse() {
        Utilisateur client = new Utilisateur("A", "B", "a@b.com", "p");
        Commande commande = new Commande(client);

        commande.setId(1L);
        commande.setEtat(Commande.EtatCommande.EXPEDIEE);
        commande.setAdresseLivraison("12 Rue Paris");

        assertThat(commande.getId()).isEqualTo(1L);
        assertThat(commande.getEtat()).isEqualTo(Commande.EtatCommande.EXPEDIEE);
        assertThat(commande.getAdresseLivraison()).isEqualTo("12 Rue Paris");
        assertThat(commande.getClient()).isEqualTo(client);
    }
}