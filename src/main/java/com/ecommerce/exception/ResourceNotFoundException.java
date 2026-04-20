package com.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)   // ← ajouter cette ligne
public class ResourceNotFoundException extends RuntimeException {


    // Construit le message d'erreur avec le nom de la ressource et son ID
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " avec id " + id + " introuvable.");
    }
}