package com.ecommerce.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import io.github.bonigarcia.wdm.WebDriverManager;

import java.time.Duration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EcommerceSystemTest {

    private static WebDriver driver;
    private static WebDriverWait wait;

    @LocalServerPort
    private int port;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @BeforeAll
    static void setUp() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @BeforeEach
    void logout() {
        driver.get(baseUrl() + "/logout");
    }

    private void loginAs(String email, String password) {
        driver.get(baseUrl() + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        driver.findElement(By.name("username")).clear();
        driver.findElement(By.name("username")).sendKeys(email);
        driver.findElement(By.name("password")).clear();
        driver.findElement(By.name("password")).sendKeys(password);
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Attendre que l'URL ne contienne plus "/login" (succès) ou contienne "?error" (échec)
        wait.until(ExpectedConditions.or(
            ExpectedConditions.not(ExpectedConditions.urlContains("/login")),
            ExpectedConditions.urlContains("?error")
        ));
    }

    @Test
    @Order(1)
    @DisplayName("ST1 - Catalogue accessible sans connexion")
    void ST1_catalogue_accessible_sans_connexion() {
        driver.get(baseUrl() + "/catalogue");
        wait.until(ExpectedConditions.urlContains("/catalogue"));
        assertTrue(driver.getCurrentUrl().contains("/catalogue"),
                "URL attendue contenant /catalogue, obtenu : " + driver.getCurrentUrl());
    }

    @Test
    @Order(2)
    @DisplayName("ST2 - Login admin réussi")
    void ST2_login_admin_reussi() {
        loginAs("admin@ecommerce.com", "admin123");
        String url = driver.getCurrentUrl();
        assertFalse(url.contains("?error"),
                "Login admin ne doit pas échouer, obtenu : " + url);
        assertTrue(url.contains("/admin") || url.contains("/catalogue"),
                "Après login admin, URL attendue /admin ou /catalogue, obtenu : " + url);
    }

    @Test
    @Order(3)
    @DisplayName("ST3 - Dashboard admin accessible")
    void ST3_dashboard_admin_accessible() {
        loginAs("admin@ecommerce.com", "admin123");
        assertFalse(driver.getCurrentUrl().contains("?error"),
                "Login admin ne doit pas échouer");

        driver.get(baseUrl() + "/admin");
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("/login")));
        assertFalse(driver.getCurrentUrl().contains("/login"),
                "Admin ne doit pas être redirigé vers /login");
    }

    @Test
    @Order(4)
    @DisplayName("ST4 - Login client réussi")
    void ST4_login_client_reussi() {
        loginAs("jean.dupont@email.com", "client123");
        String url = driver.getCurrentUrl();
        assertFalse(url.contains("?error"),
                "Login client ne doit pas échouer, obtenu : " + url);
    }

    @Test
    @Order(5)
    @DisplayName("ST5 - Client redirigé si accès admin")
    void ST5_client_redirige_acces_admin() {
        loginAs("jean.dupont@email.com", "client123");
        assertFalse(driver.getCurrentUrl().contains("?error"),
                "Login client ne doit pas échouer");

        driver.get(baseUrl() + "/admin");
        wait.until(ExpectedConditions.not(
                ExpectedConditions.urlContains("/admin")));
        String url = driver.getCurrentUrl();
        assertFalse(url.contains("/admin"),
                "Client ne doit pas accéder à /admin, obtenu : " + url);
    }

    @Test
    @Order(6)
    @DisplayName("ST6 - Ajouter produit au panier")
    void ST6_ajouter_produit_panier() {
        loginAs("jean.dupont@email.com", "client123");
        assertFalse(driver.getCurrentUrl().contains("?error"),
                "Login client ne doit pas échouer");

        driver.get(baseUrl() + "/catalogue");
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("form[action*='/panier/ajouter'] button[type='submit']")));

        driver.findElement(
                By.cssSelector("form[action*='/panier/ajouter'] button[type='submit']")
        ).click();

        wait.until(ExpectedConditions.urlContains("/panier"));
        assertTrue(driver.getCurrentUrl().contains("/panier"),
                "Après ajout, URL attendue /panier, obtenu : " + driver.getCurrentUrl());
    }

    @Test
    @Order(7)
    @DisplayName("ST7 - Login échoue avec mauvais mot de passe")
    void ST7_login_mauvais_mdp() {
        driver.get(baseUrl() + "/login");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));

        driver.findElement(By.name("username")).sendKeys("admin@ecommerce.com");
        driver.findElement(By.name("password")).sendKeys("mauvaismdp");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        wait.until(ExpectedConditions.urlContains("/login"));
        assertTrue(driver.getCurrentUrl().contains("/login"),
                "Mauvais mdp doit rester sur /login, obtenu : " + driver.getCurrentUrl());
    }
}