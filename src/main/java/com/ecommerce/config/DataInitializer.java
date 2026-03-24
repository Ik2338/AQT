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
    private String adminPassword;

    @Value("${app.init.client-password}")
    private String clientPassword;

    @Bean
    public CommandLineRunner initUsers(UtilisateurRepository userRepo,
                                       RoleRepository roleRepo,
                                       PasswordEncoder passwordEncoder) {
        return args -> {
            Role roleClient = roleRepo.findByNom("ROLE_CLIENT")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_CLIENT")));
            Role roleAdmin = roleRepo.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));

            if (userRepo.findByEmail("admin@ecommerce.com").isEmpty()) {
                Utilisateur admin = new Utilisateur("Admin", "System",
                    "admin@ecommerce.com",
                    passwordEncoder.encode(adminPassword)); // ✅ Via variable
                admin.getRoles().add(roleAdmin);
                admin.getRoles().add(roleClient);
                userRepo.save(admin);
                System.out.println("✅ Admin créé");
            }

            if (userRepo.findByEmail("jean.dupont@email.com").isEmpty()) {
                Utilisateur jean = new Utilisateur("Dupont", "Jean",
                    "jean.dupont@email.com",
                    passwordEncoder.encode(clientPassword)); // ✅ Via variable
                jean.getRoles().add(roleClient);
                userRepo.save(jean);
                System.out.println("✅ Jean créé");
            }
        };
    }
}