package com.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Config de sécurité utilisée UNIQUEMENT par les tests Selenium (EcommerceSystemTest).
 *
 * POURQUOI ce fichier séparé ?
 * Les tests Selenium démarrent un vrai Tomcat avec une vraie base H2.
 * Les users (admin, jean) sont créés par DataInitializer avec BCrypt.
 * Si on utilisait SecurityConfigTest (@Primary + InMemoryUserDetailsManager),
 * le vrai UserDetailsService serait écrasé → login échoue car BCrypt vs {noop}.
 *
 * Cette config ne définit AUCUN UserDetailsService → Spring utilise celui
 * de production (qui lit la base H2) → login fonctionne avec les vrais users.
 *
 * IMPORTANT : le log "Using generated security password" indique que
 * UserDetailsServiceAutoConfiguration s'active. Pour l'éviter, on exclut
 * cette auto-config dans EcommerceSystemTest via @SpringBootTest(properties=...).
 *
 * Elle est activée uniquement via @ActiveProfiles("selenium").
 */
@TestConfiguration
@Profile("selenium")
public class SecurityConfigSelenium {

    @Bean("seleniumSecurityFilterChain")
    public SecurityFilterChain seleniumSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/login", "/inscription", "/catalogue", "/produit/**",
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
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll()
            );

        return http.build();
    }
}