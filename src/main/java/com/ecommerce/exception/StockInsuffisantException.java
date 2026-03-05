package com.ecommerce.exception;
public class StockInsuffisantException extends RuntimeException {
    public StockInsuffisantException(String nom, int dispo, int demande) {
        super(String.format("Stock insuffisant pour '%s'. Disponible: %d, Demandé: %d", nom, dispo, demande));
    }
}
