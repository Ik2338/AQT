package com.ecommerce.service;

import java.util.Optional;

import org.springframework.context.ApplicationContext;
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
    private final RoleRepository        roleRepo;
    private final PasswordEncoder       passwordEncoder;

    /**
     * ApplicationContext utilisé pour obtenir le proxy Spring de ce service
     * et éviter la self-invocation sur les méthodes @Transactional(readOnly=true).
     */
    private final ApplicationContext    applicationContext;

    public UtilisateurService(UtilisateurRepository utilisateurRepo,
                               RoleRepository roleRepo,
                               PasswordEncoder passwordEncoder,
                               ApplicationContext applicationContext) {
        this.utilisateurRepo    = utilisateurRepo;
        this.roleRepo           = roleRepo;
        this.passwordEncoder    = passwordEncoder;
        this.applicationContext = applicationContext;
    }

    /** Retourne le proxy Spring de ce service (évite la self-invocation). */
    private UtilisateurService self() {
        return applicationContext.getBean(UtilisateurService.class);
    }

    // ─── Inscription ─────────────────────────────────────────────────────────

    public Utilisateur inscrire(Utilisateur u) {
        if (utilisateurRepo.existsByEmail(u.getEmail())) {
            throw new IllegalArgumentException("Email deja utilise : " + u.getEmail());
        }
        u.setMotDePasse(passwordEncoder.encode(u.getMotDePasse()));
        u.setActif(true);
        roleRepo.findByNom("ROLE_CLIENT").ifPresent(r -> u.getRoles().add(r));
        return utilisateurRepo.save(u);
    }

    // ─── Lecture ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    @Transactional(readOnly = true)
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    @Transactional(readOnly = true)
    public Optional<Utilisateur> trouverParEmailOptional(String email) {
        return utilisateurRepo.findByEmail(email);
    }

    // ─── Mise à jour profil ──────────────────────────────────────────────────

    /**
     * Met à jour les informations de profil d'un utilisateur.
     * Utilise self() pour appeler trouverParId via le proxy Spring
     * et garantir l'interception @Transactional(readOnly=true).
     */
    public Utilisateur mettreAJourProfil(Long id, String nom, String prenom,
                                          String telephone, String adresse) {
        Utilisateur u = self().trouverParId(id);
        u.setNom(nom);
        u.setPrenom(prenom);
        u.setTelephone(telephone);
        u.setAdresse(adresse);
        return utilisateurRepo.save(u);
    }
}