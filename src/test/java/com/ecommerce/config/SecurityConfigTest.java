package com.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de sécurité pour les tests MockMvc UNIQUEMENT (profil "test").
 *
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │  PROFIL        │ CONFIG ACTIVE           │ UserDetailsService            │
 * ├──────────────────────────────────────────────────────────────────────────┤
 * │  (aucun/prod)  │ SecurityConfig          │ BDD via UtilisateurRepository │
 * │  dev           │ SecurityConfig          │ BDD via UtilisateurRepository │
 * │  test          │ SecurityConfigTest ◄─── │ InMemory {noop}               │
 * │  selenium      │ SecurityConfigSelenium  │ BDD H2 via DataInitializer    │
 * └──────────────────────────────────────────────────────────────────────────┘
 *
 * POURQUOI CSRF EST ACTIVÉ ICI ?
 * Le template admin/produits/formulaire.html référence ${_csrf.parameterName}
 * manuellement. Quand CSRF est désactivé, Spring injecte _csrf = null →
 * Thymeleaf crash avec EL1007E: Property or field 'parameterName' cannot be
 * found on null.
 *
 * Solution retenue : garder CSRF activé dans la SecurityFilterChain de test.
 * Les tests MockMvc qui font des POST utilisent .with(csrf()) de spring-security-test
 * pour injecter le token automatiquement.
 *
 * Credentials disponibles dans tous les tests MockMvc :
 *   CLIENT → jean.dupont@email.com / client123
 *   ADMIN  → admin@ecommerce.com  / admin123
 */
@TestConfiguration
@Profile("!selenium")
public class SecurityConfigTest {


    @Bean
    @Primary
    public PasswordEncoder testPasswordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // -------------------------------------------------------------------------
    // SecurityFilterChain — CSRF activé pour supporter ${_csrf} dans Thymeleaf
    // -------------------------------------------------------------------------

    /**
     * Même règles d'autorisation que SecurityConfig.
     *
     * CSRF intentionnellement ACTIVÉ (contrairement à la version précédente) :
     * le template formulaire.html accède à ${_csrf.parameterName} manuellement.
     * Si CSRF est désactivé, _csrf = null → crash Thymeleaf en test R15.
     *
     * Pour les tests POST MockMvc, utiliser :
     *   mockMvc.perform(post("/...").with(csrf())...)
     *   import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
     */
    @Bean
    @Primary
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/inscription",
                    "/catalogue", "/produit/**",
                    "/", "/css/**", "/js/**", "/images/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/panier/**", "/commande/**", "/profil/**").hasRole("CLIENT")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/catalogue", true)
                .failureUrl("/login?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );
        return http.build();
    }

    // -------------------------------------------------------------------------
    // UserDetailsService — InMemory, {noop}, pas de BDD
    // -------------------------------------------------------------------------

    /**
     * Deux utilisateurs en mémoire correspondant aux users créés par DataInitializer.
     * Mêmes credentials, mais {noop} au lieu de BCrypt → pas de BDD nécessaire.
     */
    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        return new InMemoryUserDetailsManager(
            User.withUsername("jean.dupont@email.com")
                .password("{noop}client123")
                .roles("CLIENT")
                .build(),
            User.withUsername("admin@ecommerce.com")
                .password("{noop}admin123")
                .roles("ADMIN")
                .build()
        );
    }

    // -------------------------------------------------------------------------
    // AuthenticationManager — câblé sur les beans de test ci-dessus
    // -------------------------------------------------------------------------

    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(testUserDetailsService());
        provider.setPasswordEncoder(testPasswordEncoder());
        return new ProviderManager(provider);
    }
}