package com.ecommerce.exception;

// Exception levée quand une ressource est introuvable en base de données
public class ResourceNotFoundException extends RuntimeException {

    // Construit le message d'erreur avec le nom de la ressource et son ID
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " avec id " + id + " introuvable.");
    }
}