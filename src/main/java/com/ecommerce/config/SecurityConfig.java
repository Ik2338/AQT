package com.ecommerce.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import com.ecommerce.repository.UtilisateurRepository;

import java.io.IOException;

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
                .requestMatchers("/", "/catalogue/**", "/produit/**", "/inscription",
                        "/login", "/h2-console/**", "/css/**", "/js/**", "/images/**").permitAll()
                .requestMatchers("/panier/**", "/commande/**", "/profil/**").hasRole("CLIENT")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
        )
        .formLogin(f -> f
                .loginPage("/login")
                .successHandler(roleBasedSuccessHandler())  // ← REDIRECT SELON RÔLE
                .permitAll()
        )
        .logout(l -> l
                .logoutSuccessUrl("/login?logout")
                .permitAll()
        )
        .csrf(c -> c.ignoringRequestMatchers("/h2-console/**"))
        .headers(h -> h.frameOptions(f -> f.sameOrigin()));

        return http.build();
    }

    /**
     * Redirige après login selon le rôle :
     *   ROLE_ADMIN  → /admin
     *   ROLE_CLIENT → /catalogue
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (HttpServletRequest request,
                HttpServletResponse response,
                Authentication authentication) -> {

            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            String redirectUrl = isAdmin ? "/admin" : "/catalogue";
            response.sendRedirect(request.getContextPath() + redirectUrl);
        };
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            var u = utilisateurRepo.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Introuvable: " + email));
            var authorities = u.getRoles().stream()
                    .map(r -> new SimpleGrantedAuthority(r.getNom())).toList();
            return new User(u.getEmail(), u.getMotDePasse(), authorities);
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}