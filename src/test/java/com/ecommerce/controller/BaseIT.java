package com.ecommerce.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import com.ecommerce.config.SecurityConfigTest;

/**
 * Classe de base pour tous les tests d'intégration (*IT.java).
 *
 * CORRECTION : @Import(SecurityConfigTest.class)
 * ──────────────────────────────────────────────
 * Dans un @SpringBootTest complet, Spring charge TOUS les beans de l'application,
 * y compris le vrai UserDetailsService de production (BCrypt, via SecurityConfig).
 * DataInitializer crée les users avec passwordEncoder.encode() → BCrypt en base.
 *
 * Même avec @Primary sur SecurityConfigTest.testUserDetailsService(),
 * l'AuthenticationManager peut être construit avec le bean BCrypt en premier.
 * Résultat : POST /login avec {noop}client123 → compare contre BCrypt → ?error.
 *
 * @Import(SecurityConfigTest.class) garantit que le bean InMemoryUserDetailsManager
 * ({noop}client123 / {noop}admin123) est EXPLICITEMENT présent et @Primary dans
 * le contexte de chaque IT, avant que l'AuthenticationManager soit construit.
 *
 * @DirtiesContext(AFTER_CLASS) force un contexte frais entre classes IT pour
 * éviter toute contamination entre contextes.
 */
@SpringBootTest(properties = {
	    "spring.main.allow-bean-definition-overriding=true"
	})
	@AutoConfigureMockMvc
	@ActiveProfiles("test")
	@Import(SecurityConfigTest.class)
	@Transactional
	@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
	@Sql(
	    scripts = "/test-data.sql",
	    executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
	)
	public abstract class BaseIT {
	}
    // Classe vide — sert uniquement à partager les annotations
