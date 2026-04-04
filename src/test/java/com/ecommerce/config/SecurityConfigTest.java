package com.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@Profile("!selenium") 
public class SecurityConfigTest {

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // ── URLs publiques (pas besoin d'être connecté) ──
                .requestMatchers("/login", "/inscription", "/catalogue", "/produit/**", "/", "/css/**", "/js/**").permitAll()
                // ── Admin réservé au rôle ADMIN ──
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // ── Tout le reste nécessite une authentification ──
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
}