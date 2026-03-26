package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.ecommerce.model.Role;
import com.ecommerce.model.Utilisateur;
import com.ecommerce.repository.RoleRepository;
import com.ecommerce.repository.UtilisateurRepository;

@Configuration
public class DataInitializer {

    @Value("${app.init.admin-password}")
    private String adminPassword; // Mot de passe admin depuis application.properties

    @Value("${app.init.client-password}")
    private String clientPassword; // Mot de passe client depuis application.properties

    @Bean
    public CommandLineRunner initUsers(UtilisateurRepository userRepo,
                                       RoleRepository roleRepo,
                                       PasswordEncoder passwordEncoder) {
        // Exécuté au démarrage : initialise les rôles et utilisateurs par défaut
        return args -> {

            // Crée les rôles si inexistants
            Role roleClient = roleRepo.findByNom("ROLE_CLIENT")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_CLIENT")));
            Role roleAdmin = roleRepo.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

            // Crée l'admin par défaut si absent
            if (userRepo.findByEmail("admin@ecommerce.com").isEmpty()) {
                Utilisateur admin = new Utilisateur("Admin", "System",
                    "admin@ecommerce.com",
                    passwordEncoder.encode(adminPassword));
                admin.getRoles().add(roleAdmin);
                admin.getRoles().add(roleClient);
                userRepo.save(admin);
                System.out.println("✅ Admin créé");
            }

            // Crée un client de test si absent
            if (userRepo.findByEmail("jean.dupont@email.com").isEmpty()) {
                Utilisateur jean = new Utilisateur("Dupont", "Jean",
                    "jean.dupont@email.com",
                    passwordEncoder.encode(clientPassword));
                jean.getRoles().add(roleClient);
                userRepo.save(jean);
                System.out.println("✅ Jean créé");
            }
        };
    }
}