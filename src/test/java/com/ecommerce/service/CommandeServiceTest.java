package com.ecommerce.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import com.ecommerce.exception.StockInsuffisantException;
import com.ecommerce.model.Commande;
import com.ecommerce.model.Commande.EtatCommande;
import com.ecommerce.model.LignePanier;
import com.ecommerce.model.Panier;
import com.ecommerce.model.Produit;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.CommandeRepository;
import com.ecommerce.repository.ProduitRepository;

class CommandeServiceTest {

    private CommandeRepository commandeRepo;
    private ProduitRepository  produitRepo;
    private CommandeService    commandeService;
    private ApplicationContext applicationContext;

    private Utilisateur client;
    private Produit     produit;
    private Panier      panier;

    @BeforeEach
    void setUp() {
        commandeRepo       = mock(CommandeRepository.class);
        produitRepo        = mock(ProduitRepository.class);
        applicationContext = mock(ApplicationContext.class);

        PanierService panierService = new PanierService(null, null) {
            @Override
            public void vider(Utilisateur utilisateur) { /* no-op */ }
        };

        commandeService = new CommandeService(
                commandeRepo, produitRepo, panierService, applicationContext);

        // Le proxy self() doit retourner le service lui-même pour les tests unitaires
        when(applicationContext.getBean(CommandeService.class)).thenReturn(commandeService);

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

    // R1 – panier vide lève une exception
    @Test
    @DisplayName("R1 - panier vide leve une exception")
    void R1_panierVide_leveException() {
        Panier panierVide = new Panier();
        panierVide.setUtilisateur(client);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> commandeService.validerPanier(client, panierVide, "Adresse test"));

        assertEquals("Le panier est vide.", ex.getMessage());
    }

    // R2 – quantité > stock lève StockInsuffisantException
    @Test
    @DisplayName("R2 - quantite superieure au stock leve StockInsuffisantException")
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

    // R3 – quantité OK → commande créée avec état VALIDEE
    @Test
    @DisplayName("R3 - quantite ok, commande creee avec etat VALIDEE")
    void R3_quantiteOk_commandeCreee_etatVALIDEE() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Commande result = commandeService.validerPanier(client, panier, "12 Rue test");

        assertNotNull(result);
        assertEquals(EtatCommande.VALIDEE, result.getEtat());
    }

    // R4 – commande créée contient l'adresse de livraison
    @Test
    @DisplayName("R4 - commande creee contient l adresse de livraison")
    void R4_commandeCreee_contientAdresseLivraison() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Commande result = commandeService.validerPanier(client, panier, "12 Rue de la Paix");

        assertNotNull(result);
        assertEquals("12 Rue de la Paix", result.getAdresseLivraison());
    }

    // R5 – validation commande décrémente le stock
    @Test
    @DisplayName("R5 - validation commande decremente le stock du produit")
    void R5_validationCommande_decrementeStock() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        int stockInitial = produit.getStock(); // 10

        commandeService.validerPanier(client, panier, "12 Rue test");

        assertEquals(stockInitial - 2, produit.getStock()); // 8
        verify(produitRepo, times(1)).save(produit);
    }

    // R6 – changerEtat met à jour l'état via le proxy self()
    @Test
    @DisplayName("R6 - changerEtat met a jour l etat de la commande")
    void R6_changerEtat_metAJourEtat() {
        when(commandeRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        Commande commande = new Commande(client);
        commande.setId(1L);
        commande.setEtat(EtatCommande.VALIDEE);
        when(commandeRepo.findById(1L)).thenReturn(java.util.Optional.of(commande));

        Commande result = commandeService.changerEtat(1L, EtatCommande.EXPEDIEE);

        assertEquals(EtatCommande.EXPEDIEE, result.getEtat());
        verify(commandeRepo).save(commande);
    }
}