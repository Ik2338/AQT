package com.ecommerce.service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

@Service @Transactional
public class UtilisateurService {
    private final UtilisateurRepository utilisateurRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public UtilisateurService(UtilisateurRepository utilisateurRepo, RoleRepository roleRepo, PasswordEncoder passwordEncoder) {
        this.utilisateurRepo=utilisateurRepo; this.roleRepo=roleRepo; this.passwordEncoder=passwordEncoder;
    }

    public Utilisateur inscrire(Utilisateur u) {
        if (utilisateurRepo.existsByEmail(u.getEmail())) {
			throw new IllegalArgumentException("Email deja utilise : " + u.getEmail());
		}
        u.setMotDePasse(passwordEncoder.encode(u.getMotDePasse()));
        u.setActif(true);
        roleRepo.findByNom("ROLE_CLIENT").ifPresent(r -> u.getRoles().add(r));
        return utilisateurRepo.save(u);
    }

    @Transactional(readOnly=true)
    public Utilisateur trouverParId(Long id) {
        return utilisateurRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Utilisateur", id));
    }

    @Transactional(readOnly=true)
    public Utilisateur trouverParEmail(String email) {
        return utilisateurRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
    }

    public Utilisateur mettreAJourProfil(Long id, String nom, String prenom, String telephone, String adresse) {
        Utilisateur u = trouverParId(id);
        u.setNom(nom); u.setPrenom(prenom); u.setTelephone(telephone); u.setAdresse(adresse);
        return utilisateurRepo.save(u);
    }
}
