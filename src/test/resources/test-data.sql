DELETE FROM ligne_commande;
DELETE FROM commande;
DELETE FROM lignes_panier;
DELETE FROM panier;
DELETE FROM produit;
DELETE FROM categorie;
DELETE FROM utilisateur_role;
DELETE FROM utilisateur;
DELETE FROM role;

INSERT INTO role (id, nom) VALUES (1, 'ROLE_ADMIN');
INSERT INTO role (id, nom) VALUES (2, 'ROLE_CLIENT');

INSERT INTO categorie (id, nom) VALUES (1, 'Electronique');
INSERT INTO categorie (id, nom) VALUES (2, 'Vetements');
INSERT INTO categorie (id, nom) VALUES (3, 'Livres');

INSERT INTO produit (id, nom, description, prix, stock, actif, categorie_id, image_url)
VALUES (1, 'Smartphone Samsung Galaxy', 'Smartphone Android haut de gamme', 
        599.99, 10, true, 1, 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400');

INSERT INTO produit (id, nom, description, prix, stock, actif, categorie_id, image_url)
VALUES (2, 'Laptop Dell XPS', 'Ordinateur portable professionnel', 
        1299.99, 5, true, 1, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400');

INSERT INTO produit (id, nom, description, prix, stock, actif, categorie_id, image_url)
VALUES (3, 'T-shirt Nike', 'T-shirt sport confortable', 
        29.99, 20, true, 2, 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400');

ALTER TABLE role ALTER COLUMN id RESTART WITH 3;
ALTER TABLE categorie ALTER COLUMN id RESTART WITH 4;
ALTER TABLE produit ALTER COLUMN id RESTART WITH 4;