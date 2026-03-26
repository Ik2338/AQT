package com.ecommerce.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

@Service
@Transactional
public class UtilisateurService {

    private final UtilisateurRepository utilisateurRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    // Injection des dépendances via constructeur
    public UtilisateurService(UtilisateurRepository utilisateurRepo,
                               RoleRepository roleRepo,
                               PasswordEncoder passwordEncoder) {
        this.utilisateurRepo = utilisateurRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    // Inscrit un nouvel utilisateur avec le rôle CLIENT et mot de passe chiffré
    public Utilisateur inscrire(Utilisateur u) {
        // Vérifie que l'email n'est pas déjà utilisé
        if (utilisateurRepo.existsByEmail(u.getEmail())) {
            throw new IllegalArgumentException("Email deja utilise : " + u.getEmail());
        }
        u.setMotDePasse(passwordEncoder.encode(u.getMotDePasse())); // Chiffre le mot de passe
        u.setActif(true);
        roleRepo.findByNom("ROLE_CLIENT").ifPresent(r -> u.getRoles().add(r)); // Attribue le rôle CLIENT
        return utilisateurRepo.save(u);
    }

    // Recherche un utilisateur par ID, lève une exception si introuvable
    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    // Recherche un utilisateur par email (utilisé pour l'authentification)
    @Transactional(readOnly = true)
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    // Met à jour les informations du profil d'un utilisateur
    public Utilisateur mettreAJourProfil(Long id, String nom, String prenom,
                                          String telephone, String adresse) {
        Utilisateur u = trouverParId(id);
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setTelephone(telephone);
        u.setAdresse(adresse);
        return utilisateurRepo.save(u);
    }
}