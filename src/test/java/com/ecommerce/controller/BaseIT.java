package com.ecommerce.controller;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

/**
 * Classe de base pour tous les tests d'intégration.
 *
 * Chaque fichier *IT.java étend cette classe pour hériter de :
 *   - @SpringBootTest     → Spring démarre complet avec H2
 *   - @AutoConfigureMockMvc → MockMvc injecté automatiquement
 *   - @ActiveProfiles("test") → utilise application-test.properties
 *   - @Transactional     → rollback automatique après chaque test
 *   - @Sql               → charge test-data.sql avant chaque test
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public abstract class BaseIT {
    // Classe vide — sert uniquement à partager les annotations
}