INSERT INTO categorie (id, nom) VALUES (1, 'Electronique');
INSERT INTO categorie (id, nom) VALUES (2, 'Vetements');
INSERT INTO categorie (id, nom) VALUES (3, 'Livres');
INSERT INTO categorie (id, nom) VALUES (4, 'Sport');
INSERT INTO categorie (id, nom) VALUES (5, 'Maison');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Smartphone Samsung Galaxy', 'Smartphone Android 128Go', 599.99, 50, TRUE, 1);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Laptop Dell XPS 15', 'Intel i7, 16Go RAM, SSD 512Go', 1299.99, 20, TRUE, 1);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Ecouteurs Sony WH-1000XM5', 'Casque sans fil reduction bruit', 349.99, 35, TRUE, 1);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('T-Shirt Premium Coton', 'T-Shirt 100% coton bio', 29.99, 100, TRUE, 2);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Veste en Jean Classic', 'Veste denim vintage', 89.99, 45, TRUE, 2);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Java 17 - Guide Complet', 'Reference Java moderne', 45.00, 30, TRUE, 3);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Spring Boot en Action', 'Spring Boot 3', 52.00, 25, TRUE, 3);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Velo de Course Carbon', 'Cadre carbone leger', 1899.99, 8, TRUE, 4);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Tapis de Yoga Premium', 'Tapis antiderapant 6mm', 49.99, 60, TRUE, 4);
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id) VALUES ('Machine a Cafe Deluxe', 'Cafetiere avec broyeur', 299.99, 15, TRUE, 5);