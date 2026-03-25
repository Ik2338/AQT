package com.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.ecommerce.repository.UtilisateurRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UtilisateurRepository utilisateurRepo;

    public SecurityConfig(UtilisateurRepository utilisateurRepo) {
        this.utilisateurRepo = utilisateurRepo;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
                // Accès public
                .requestMatchers("/", "/catalogue/**", "/produit/**",
                                 "/inscription", "/login",
                                 "/css/**", "/js/**", "/images/**").permitAll()
                // Réservé aux clients connectés
                .requestMatchers("/panier/**", "/commande/**", "/profil/**").hasRole("CLIENT")
                // Réservé aux admins
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
        .formLogin(f -> f.loginPage("/login")
                         .defaultSuccessUrl("/catalogue", true)
                         .permitAll())
        .logout(l -> l.logoutSuccessUrl("/catalogue").permitAll())
        .headers(h -> h.frameOptions(f -> f.sameOrigin())); // Permet les iframes same-origin

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Charge l'utilisateur par email et construit son objet Spring Security
        return email -> {
            var u = utilisateurRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Introuvable: " + email));
            var authorities = u.getRoles().stream()
                .map(r -> new SimpleGrantedAuthority(r.getNom())).toList();
            return new User(u.getEmail(), u.getMotDePasse(), authorities);
        };
    }

    // Encodage BCrypt pour les mots de passe
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}