# Site E-Commerce — Projet AQL 2025/2026

Projet fil rouge du module Assurance Qualite Logicielle et Automatisation des Tests.
Encadrant : Pr. Salima CHANTIT — Departement Informatique FSTM

---

## Description

Application web e-commerce developpee avec Spring Boot selon une architecture MVC.
Elle couvre la gestion du catalogue, du panier, des commandes et un espace d'administration complet.

---

## Technologies

- Java 23
- Spring Boot 3.3.5
- Spring Security 6
- Spring Data JPA / Hibernate
- Thymeleaf 3.1
- H2 (base de donnees en memoire)
- Maven
- JUnit 5
- Mockito
- JaCoCo 0.8.11

---

## Acteurs

- Visiteur : consulter le catalogue, s'inscrire
- Client : s'authentifier, gerer le panier, passer des commandes
- Administrateur : gerer les produits, categories, stocks et commandes

---

## Regles metier

- R1 : Un client doit etre authentifie pour passer une commande
- R2 : La quantite commandee ne peut pas depasser le stock disponible
- R3 : Une commande validee est definitive
- R4 : Un produit supprime ne s'affiche plus dans le catalogue
- R5 : Une commande validee decremente automatiquement le stock

---

## Credentials par defaut

| Role           | Email                  | Mot de passe |
|----------------|------------------------|--------------|
| Administrateur | admin@ecommerce.com    | admin123     |
| Client         | jean.dupont@email.com  | client123    |

---

## Lancer le projet

1. Importer le projet dans Eclipse (Import > Existing Maven Project)
2. Clic droit sur le projet > Run As > Spring Boot App
3. Ouvrir le navigateur : http://localhost:8081

---

## Lancer les tests

Clic droit sur le projet > Run As > JUnit Test

Ou via Maven :

    mvn test

---

## Rapport de couverture JaCoCo

    mvn verify

Le rapport est disponible dans : target/site/jacoco/index.html

---

## Structure du projet

    src/main/java/com/ecommerce/
        config/       - SecurityConfig, DataInitializer
        controller/   - AdminController, CatalogueController, PanierController...
        service/      - ProduitService, CommandeService, PanierService...
        model/        - Produit, Commande, Panier, Utilisateur...
        repository/   - ProduitRepository, CommandeRepository...
        exception/    - StockInsuffisantException, ResourceNotFoundException

    src/test/java/com/ecommerce/
        service/      - ProduitServiceTest, CommandeServiceTest
        controller/   - AdminControllerTest, CatalogueControllerTest

---

## Tests

| Fichier                 | Type        | Tests | Statut |
|-------------------------|-------------|-------|--------|
| ProduitServiceTest      | Unitaire    | 5     | PASS   |
| CommandeServiceTest     | Unitaire    | 5     | PASS   |
| AdminControllerTest     | Integration | 7     | PASS   |
| CatalogueControllerTest | Integration | 3     | PASS   |
| Total                   |             | 20    | 20/20  |