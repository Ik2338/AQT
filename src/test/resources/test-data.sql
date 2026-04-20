-- ============================================================
-- test-data.sql  —  src/test/resources/test-data.sql
-- ============================================================

SET REFERENTIAL_INTEGRITY FALSE;

-- Utilise DELETE au lieu de TRUNCATE (plus tolérant si table absente)
DELETE FROM ligne_commande WHERE 1=1;
DELETE FROM commande WHERE 1=1;
DELETE FROM utilisateur_role WHERE 1=1;
DELETE FROM utilisateur WHERE 1=1;
DELETE FROM role WHERE 1=1;
DELETE FROM produit WHERE 1=1;
DELETE FROM categorie WHERE 1=1;

SET REFERENTIAL_INTEGRITY TRUE;

-- Reset séquences
ALTER TABLE categorie   ALTER COLUMN id RESTART WITH 1;
ALTER TABLE produit     ALTER COLUMN id RESTART WITH 1;
ALTER TABLE role        ALTER COLUMN id RESTART WITH 1;
ALTER TABLE utilisateur ALTER COLUMN id RESTART WITH 1;

-- Rôles
INSERT INTO role (nom) VALUES ('ROLE_CLIENT');
INSERT INTO role (nom) VALUES ('ROLE_ADMIN');

-- Utilisateurs
INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, actif, telephone, adresse)
VALUES ('Dupont', 'Jean', 'jean.dupont@email.com', '{noop}client123', TRUE, '0600000000', '12 Rue de la Paix');

INSERT INTO utilisateur (nom, prenom, email, mot_de_passe, actif, telephone, adresse)
VALUES ('Admin', 'Super', 'admin@ecommerce.com', '{noop}admin123', TRUE, '0600000001', '1 Rue Admin');

-- Rôles utilisateurs
INSERT INTO utilisateur_role (utilisateur_id, role_id) VALUES (1, 1);
INSERT INTO utilisateur_role (utilisateur_id, role_id) VALUES (2, 1);
INSERT INTO utilisateur_role (utilisateur_id, role_id) VALUES (2, 2);

-- Catégories
INSERT INTO categorie (nom) VALUES ('Electronique');
INSERT INTO categorie (nom) VALUES ('Vetements');
INSERT INTO categorie (nom) VALUES ('Livres');
INSERT INTO categorie (nom) VALUES ('Sport');
INSERT INTO categorie (nom) VALUES ('Maison');

-- Produits
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Smartphone Samsung Galaxy', 'Smartphone Android 128Go', 599.99, 50, TRUE, 1, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Laptop Dell XPS 15', 'Intel i7, 16Go RAM, SSD 512Go', 1299.99, 20, TRUE, 1, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('T-Shirt Nike', 'T-shirt sport respirant', 29.99, 100, TRUE, 2, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Clean Code', 'Guide du code propre', 39.99, 15, TRUE, 3, 'https://images.unsplash.com/photo-1589998059171-988d887df646?w=400');