package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
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
@Profile({"default", "selenium"})
/*
 * "default"  → application réelle → users créés en BCrypt → site fonctionne
 * "selenium" → tests Selenium     → users créés en BCrypt → login Selenium fonctionne
 * "test"     → DataInitializer NE TOURNE PAS
 *              → SecurityConfigTest fournit les users {noop} → AuthControllerIT passe
 */
public class DataInitializer implements ApplicationRunner {

    private final UtilisateurRepository utilisateurRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.init.admin-password:admin123}")
    private String adminPassword;

    @Value("${app.init.client-password:client123}")
    private String clientPassword;

    public DataInitializer(UtilisateurRepository utilisateurRepo,
                           RoleRepository roleRepo,
                           PasswordEncoder passwordEncoder) {
        this.utilisateurRepo = utilisateurRepo;
        this.roleRepo = roleRepo;
        this.passwordEncoder = passwordEncoder;
    }
    private static final org.slf4j.Logger log = 
    	    org.slf4j.LoggerFactory.getLogger(DataInitializer.class);

    @Override
    public void run(ApplicationArguments args) {
        Role roleAdmin = roleRepo.findByNom("ROLE_ADMIN")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_ADMIN")));
        Role roleClient = roleRepo.findByNom("ROLE_CLIENT")
                .orElseGet(() -> roleRepo.save(new Role("ROLE_CLIENT")));

        if (!utilisateurRepo.existsByEmail("admin@ecommerce.com")) {
            Utilisateur admin = new Utilisateur("Admin", "Super",
                    "admin@ecommerce.com",
                    passwordEncoder.encode(adminPassword));
            admin.setActif(true);
            admin.getRoles().add(roleAdmin);
            utilisateurRepo.save(admin);
            log.info("Admin créé : admin@ecommerce.com");
        }

        if (!utilisateurRepo.existsByEmail("jean.dupont@email.com")) {
            Utilisateur jean = new Utilisateur("Dupont", "Jean",
                    "jean.dupont@email.com",
                    passwordEncoder.encode(clientPassword));
            jean.setActif(true);
            jean.getRoles().add(roleClient);
            utilisateurRepo.save(jean);
            log.info("Admin créé : admin@ecommerce.com");
        }
    }
}