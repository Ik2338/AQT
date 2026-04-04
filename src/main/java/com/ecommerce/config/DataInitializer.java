package com.ecommerce.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ecommerce.model.Role;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

@Component
@Profile("test")
public class DataInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UtilisateurRepository utilisateurRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepo = utilisateurRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        // Crée les rôles si absents
        Role roleAdmin = roleRepo.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));
        Role roleClient = roleRepo.findByNom("ROLE_CLIENT")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_CLIENT")));

        // Crée l'admin si absent
        if (!utilisateurRepo.existsByEmail("admin@ecommerce.com")) {
            Utilisateur admin = new Utilisateur("Admin", "Super",
                    "admin@ecommerce.com",
                    passwordEncoder.encode("admin123"));
            admin.setActif(true);
            admin.getRoles().add(roleAdmin);
            utilisateurRepo.save(admin);
            System.out.println("✅ Admin créé");
        }

        // Crée jean.dupont avec le mot de passe "client123"
        if (!utilisateurRepo.existsByEmail("jean.dupont@email.com")) {
            Utilisateur jean = new Utilisateur("Dupont", "Jean",
                    "jean.dupont@email.com",
                    passwordEncoder.encode("client123"));
            jean.setActif(true);
            jean.getRoles().add(roleClient);
            utilisateurRepo.save(jean);
            System.out.println("✅ Jean créé");
        }
    }
}