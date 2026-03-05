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

@Configuration @EnableWebSecurity
public class SecurityConfig {
    private final UtilisateurRepository utilisateurRepo;
    public SecurityConfig(UtilisateurRepository utilisateurRepo) { this.utilisateurRepo = utilisateurRepo; }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/","/catalogue/**","/produit/**","/inscription","/login",
                             "/h2-console/**","/css/**","/js/**","/images/**").permitAll()
            .requestMatchers("/panier/**","/commande/**","/profil/**").hasRole("CLIENT")
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated()
        )
        .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/catalogue",true).permitAll())
        .logout(l -> l.logoutSuccessUrl("/catalogue").permitAll())
        .csrf(c -> c.ignoringRequestMatchers("/h2-console/**"))
        .headers(h -> h.frameOptions(f -> f.sameOrigin()));
        return http.build();
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

    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
