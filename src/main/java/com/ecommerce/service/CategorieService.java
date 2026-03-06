package com.ecommerce.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Categorie;
import com.ecommerce.repository.CategorieRepository;

@Service @Transactional
public class CategorieService {

    private final CategorieRepository categorieRepo;

    public CategorieService(CategorieRepository categorieRepo) {
        this.categorieRepo = categorieRepo;
    }

    @Transactional(readOnly = true)
    public List<Categorie> listerToutes() {
        return categorieRepo.findAll();
    }

    @Transactional(readOnly = true)
    public Categorie trouverParId(Long id) {
        return categorieRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categorie", id));
    }

    public Categorie creer(String nom) {
        if (nom == null || nom.isBlank()) {
            throw new IllegalArgumentException("Le nom de la catégorie est obligatoire.");
        }
        Categorie cat = new Categorie();
        cat.setNom(nom.trim());
        return categorieRepo.save(cat);
    }

    public void supprimer(Long id) {
        if (!categorieRepo.existsById(id)) {
            throw new ResourceNotFoundException("Categorie", id);
        }
        categorieRepo.deleteById(id);
    }
}