package com.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.ecommerce.repository.UtilisateurRepository;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String URL_CATALOGUE = "/catalogue";
    private static final String URL_LOGIN     = "/login";
    private static final String URL_ADMIN     = "/admin/dashboard";

    // H2 console — UNIQUEMENT activée en développement (spring.h2.console.enabled=false en prod).
    // L'exclusion CSRF sur ce chemin est intentionnelle et sûre car :
    //   1. spring.h2.console.enabled=false en production ET en test
    //   2. /h2-console n'est accessible qu'en local (jamais exposé en prod)
    //   3. L'exclusion est conditionnelle : uniquement si profil dev actif
    // SonarQube Hotspot S4502 → reviewed, status: Safe.
    private static final String H2_CONSOLE = "/h2-console/**";

    private final Environment env;

    public SecurityConfig(Environment env) {
        this.env = env;
    }

    // -------------------------------------------------------------------------
    // Chaîne de filtres — profil production + dev uniquement
    // (test     → SecurityConfigTest.java      gère sa propre chaîne)
    // (selenium → SecurityConfigSelenium.java  gère sa propre chaîne)
    // -------------------------------------------------------------------------

    @Bean
    @Profile("!test & !selenium")
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // H2 + exclusion CSRF uniquement sur profil dev local
        boolean isDevProfile = Arrays.stream(env.getActiveProfiles())
            .anyMatch(p -> p.equalsIgnoreCase("dev"));

        http
            .authorizeHttpRequests(auth -> {
                auth.requestMatchers(
                    "/", URL_CATALOGUE, "/produit/**",
                    URL_LOGIN, "/inscription",
                    "/css/**", "/js/**", "/images/**"
                ).permitAll();

                // H2 console — accessible uniquement en dev local
                if (isDevProfile) {
                    auth.requestMatchers(H2_CONSOLE).permitAll();
                }

                auth
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .requestMatchers("/panier/**", "/profil/**", "/commande/**").hasRole("CLIENT")
                    .anyRequest().authenticated();
            })
            .formLogin(form -> form
                .loginPage(URL_LOGIN)
                .loginProcessingUrl(URL_LOGIN)
                // Redirection rôle-dépendante : ADMIN → /admin/dashboard, CLIENT → /catalogue
                .successHandler(roleBasedSuccessHandler())
                .failureUrl(URL_LOGIN + "?error")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl(URL_CATALOGUE)
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin())
            )
            .csrf(csrf -> {
                // Exclusion CSRF conditionnelle — uniquement profil dev.
                // Profils test et selenium gèrent leur propre CSRF (désactivé).
                // SonarQube Hotspot S4502 → reviewed, status: Safe.
                if (isDevProfile) {
                    csrf.ignoringRequestMatchers(H2_CONSOLE); // NOSONAR java:S4502
                }
            });

        return http.build();
    }

    // -------------------------------------------------------------------------
    // Handler de redirection post-login selon le rôle
    // -------------------------------------------------------------------------

    /**
     * Redirige l'utilisateur vers la bonne page selon son rôle après connexion.
     *   ADMIN  → /admin/dashboard
     *   CLIENT → /catalogue
     *
     * Déclaré en @Bean pour être testable indépendamment.
     * Utilisé uniquement par filterChain (prod/dev).
     *
     * SecurityConfigSelenium utilise defaultSuccessUrl("/catalogue") — intentionnel :
     * les tests Selenium ST2/ST4 vérifient l'URL après login, pas le dashboard admin.
     */
    @Bean
    @Profile("!test & !selenium")
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) -> {

            String redirectUrl = authentication.getAuthorities().contains(
                    new SimpleGrantedAuthority("ROLE_ADMIN"))
                ? URL_ADMIN
                : URL_CATALOGUE;

            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }

    // -------------------------------------------------------------------------
    // UserDetailsService — base de données
    // -------------------------------------------------------------------------

    /**
     * Charge les utilisateurs depuis la base de données (prod/dev uniquement).
     *
     * Exclu des profils "test" et "selenium" :
     *   - "test"     → SecurityConfigTest fournit un InMemoryUserDetailsManager {noop}
     *   - "selenium" → DataInitializer crée les vrais users BCrypt en H2,
     *                  Spring Boot les résout automatiquement sans ce bean
     *
     * Sans @Profile("!test & !selenium"), Spring trouve plusieurs UserDetailsService
     * → AuthenticationManager utilise le mauvais → logins silencieusement rejetés
     * (c'était la cause racine des échecs ST2 et ST4).
     */
    @Bean
    @Profile("!test & !selenium")
    public UserDetailsService userDetailsService(UtilisateurRepository utilisateurRepo) {
        return username -> utilisateurRepo.findByEmail(username)
            .map(u -> org.springframework.security.core.userdetails.User
                .withUsername(u.getEmail())
                .password(u.getMotDePasse())
                .roles(u.getRoles().stream()
                    .map(r -> r.getNom().replace("ROLE_", ""))
                    .toArray(String[]::new))
                .accountExpired(false)
                .disabled(!u.isActif())
                .build())
            .orElseThrow(() -> new UsernameNotFoundException(
                "Utilisateur introuvable : " + username));
    }

    // -------------------------------------------------------------------------
    // PasswordEncoder — partagé prod + selenium
    // -------------------------------------------------------------------------

    /**
     * BCryptPasswordEncoder utilisé en production et en Selenium.
     * DataInitializer encode les mots de passe avec ce bean.
     *
     * Exclu du profil "test" : SecurityConfigTest déclare son propre
     * PasswordEncoder (@Primary) avec DelegatingPasswordEncoder
     * pour supporter les passwords {noop} en mémoire.
     */
    @Bean
    @Profile("!test")
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}