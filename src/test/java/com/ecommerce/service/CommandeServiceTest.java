package com.ecommerce.service;

import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.*;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandeServiceTest {

    private CommandeRepository commandeRepo;
    private ProduitRepository  produitRepo;
    private CommandeService    commandeService;

    private Utilisateur client;
    private Produit     produit;
    private Panier      panier;

    @BeforeEach
    void setUp() {
        commandeRepo  = mock(CommandeRepository.class);
        produitRepo   = mock(ProduitRepository.class);

        // Java 23 : sous-classe anonyme au lieu de mock()
        PanierService panierService = new PanierService(null, null) {
            @Override
            public void vider(Utilisateur utilisateur) { /* ne rien faire */ }
        };

        commandeService = new CommandeService(commandeRepo, produitRepo, panierService);

        client = new Utilisateur();
        client.setNom("Dupont");
        client.setPrenom("Jean");
        client.setEmail("jean@test.com");
        client.setMotDePasse("pass");

        produit = new Produit();
        produit.setId(1L);
        produit.setNom("Smartphone");
        produit.setPrix(599.99);
        produit.setStock(10);
        produit.setActif(true);

        panier = new Panier();
        panier.setUtilisateur(client);

        LignePanier ligne = new LignePanier();
        ligne.setPanier(panier);
        ligne.setProduit(produit);
        ligne.setQuantite(2);
        panier.getLignes().add(ligne);
    }

    @Test
    @DisplayName("R1 - panier vide lève une exception")
    void R1_panierVide_leveException() {
        Panier panierVide = new Panier();
        panierVide.setUtilisateur(client);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commandeService.validerPanier(client, panierVide, "Adresse test"));

        assertEquals("Le panier est vide.", ex.getMessage());
    }

    @Test
    @DisplayName("R2 - quantité > stock lève StockInsuffisantException")
    void R2_quantiteDepasseStock_leveException() {
        Produit p = new Produit();
        p.setId(2L);
        p.setNom("Laptop");
        p.setPrix(999.99);
        p.setStock(1);
        p.setActif(true);

        Panier panierTest = new Panier();
        panierTest.setUtilisateur(client);

        LignePanier ligne = new LignePanier();
        ligne.setPanier(panierTest);
        ligne.setProduit(p);
        ligne.setQuantite(5);
        panierTest.getLignes().add(ligne);

        assertThrows(StockInsuffisantException.class,
                () -> commandeService.validerPanier(client, panierTest, "Adresse test"));
    }

    @Test
    @DisplayName("R2 - quantité <= stock, commande créée")
    void R2_quantiteOk_commandeCreee() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Commande result = commandeService.validerPanier(client, panier, "12 Rue test");

        assertNotNull(result);
        assertEquals(EtatCommande.VALIDEE, result.getEtat());
    }

    @Test
    @DisplayName("R3 - commande validée a l'état VALIDEE")
    void R3_commandeValidee_etatEstValidee() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Commande result = commandeService.validerPanier(client, panier, "12 Rue test");

        assertEquals(EtatCommande.VALIDEE, result.getEtat());
    }

    @Test
    @DisplayName("R5 - validation commande décrémente le stock")
    void R5_validationCommande_decrementeStock() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        int stockInitial = produit.getStock();

        commandeService.validerPanier(client, panier, "12 Rue test");

        assertEquals(stockInitial - 2, produit.getStock());
        verify(produitRepo, times(1)).save(produit);
    }
}