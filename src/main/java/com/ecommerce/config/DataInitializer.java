package com.ecommerce.config;

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

    @Bean
    public CommandLineRunner initUsers(UtilisateurRepository userRepo,
                                      RoleRepository roleRepo,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            // Créer les rôles s'ils n'existent pas
            Role roleClient = roleRepo.findByNom("ROLE_CLIENT")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_CLIENT")));
            Role roleAdmin = roleRepo.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

            // Créer admin
            if (userRepo.findByEmail("admin@ecommerce.com").isEmpty()) {
                Utilisateur admin = new Utilisateur("Admin", "System", "admin@ecommerce.com",
                    passwordEncoder.encode("admin123"));
                admin.getRoles().add(roleAdmin);
                admin.getRoles().add(roleClient);
                userRepo.save(admin);
                System.out.println("✅ Admin créé");
            }

            // Créer Jean
            if (userRepo.findByEmail("jean.dupont@email.com").isEmpty()) {
                Utilisateur jean = new Utilisateur("Dupont", "Jean", "jean.dupont@email.com",
                    passwordEncoder.encode("client123"));
                jean.getRoles().add(roleClient);
                userRepo.save(jean);
                System.out.println("✅ Jean créé");
            }


        };
    }
}