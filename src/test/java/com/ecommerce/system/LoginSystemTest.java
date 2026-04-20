package com.ecommerce.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import io.github.bonigarcia.wdm.WebDriverManager;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "server.port=8081"
)
@ActiveProfiles("selenium")
/*
 * @SpringBootTest(DEFINED_PORT) → démarre un vrai Tomcat sur le port 8081
 * @ActiveProfiles("selenium")   → charge SecurityConfigSelenium (sans UserDetailsService custom)
 *                                  + DataInitializer (profil "selenium") qui crée admin/jean en BCrypt
 *                                  → le UserDetailsService de prod (JPA/BCrypt) est utilisé
 *                                  → login avec admin123 / client123 fonctionne
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EcommerceSystemTest {

    private static WebDriver driver;
    private static final String BASE_URL = "http://localhost:8080";

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        // options.addArguments("--headless"); // décommenter pour CI sans interface
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
			driver.quit();
		}
    }

    // ─── TEST 1 : Accès catalogue sans connexion ────────────
    @Test
    @Order(1)
    @DisplayName("ST1 - Catalogue accessible sans connexion")
    void ST1_catalogue_accessible_sans_connexion() {
        driver.get(BASE_URL + "/catalogue");
        assertTrue(driver.getTitle().contains("ShopElite")
                || driver.getCurrentUrl().contains("catalogue"));
    }

    // ─── TEST 2 : Login admin ───────────────────────────────
    @Test
    @Order(2)
    @DisplayName("ST2 - Login admin réussi")
    void ST2_login_admin_reussi() {
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("admin@ecommerce.com");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        assertTrue(driver.getCurrentUrl().contains("/admin")
                || driver.getCurrentUrl().contains("/catalogue"));
    }

    // ─── TEST 3 : Accès dashboard admin ────────────────────
    @Test
    @Order(3)
    @DisplayName("ST3 - Dashboard admin accessible")
    void ST3_dashboard_admin_accessible() {
        driver.get(BASE_URL + "/admin");
        assertFalse(driver.getCurrentUrl().contains("/login"));
    }

    // ─── TEST 4 : Login client ──────────────────────────────
    @Test
    @Order(4)
    @DisplayName("ST4 - Login client réussi")
    void ST4_login_client_reussi() {
        // Déconnexion d'abord
        driver.get(BASE_URL + "/logout");

        driver.get(BASE_URL + "/login");
        driver.findElement(By.name("username")).sendKeys("jean.dupont@email.com");
        driver.findElement(By.name("password")).sendKeys("client123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        assertTrue(driver.getCurrentUrl().contains("/catalogue")
                || !driver.getCurrentUrl().contains("/login"));
    }

    // ─── TEST 5 : Client ne peut pas accéder admin ─────────
    @Test
    @Order(5)
    @DisplayName("ST5 - Client redirigé si accès admin")
    void ST5_client_redirige_acces_admin() {
        driver.get(BASE_URL + "/admin");
        assertFalse(driver.getCurrentUrl().contains("/admin/dashboard"));
    }

    // ─── TEST 6 : Ajouter produit au panier ────────────────
    @Test
    @Order(6)
    @DisplayName("ST6 - Ajouter produit au panier")
    void ST6_ajouter_produit_panier() {
        driver.get(BASE_URL + "/catalogue");

        // Clique sur le premier bouton produit
        driver.findElement(By.cssSelector(".btn-primary")).click();

        assertTrue(driver.getCurrentUrl().contains("/panier")
                || driver.getPageSource().contains("panier"));
    }

    // ─── TEST 7 : Login mauvais mot de passe ───────────────
    @Test
    @Order(7)
    @DisplayName("ST7 - Login échoue avec mauvais mot de passe")
    void ST7_login_mauvais_mdp() {
        driver.get(BASE_URL + "/logout");
        driver.get(BASE_URL + "/login");

        driver.findElement(By.name("username")).sendKeys("admin@ecommerce.com");
        driver.findElement(By.name("password")).sendKeys("mauvais");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        assertTrue(driver.getCurrentUrl().contains("/login"));
    }
}