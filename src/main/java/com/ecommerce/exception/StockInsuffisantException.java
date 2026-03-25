package com.ecommerce.exception;

// Exception levée quand la quantité demandée dépasse le stock disponible
public class StockInsuffisantException extends RuntimeException {

    // Construit le message d'erreur avec le nom du produit, stock dispo et quantité demandée
    public StockInsuffisantException(String nom, int dispo, int demande) {
        super(String.format("Stock insuffisant pour '%s'. Disponible: %d, Demandé: %d",
              nom, dispo, demande));
    }
}