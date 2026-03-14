INSERT INTO categorie (nom) VALUES ('Electronique');
INSERT INTO categorie (nom) VALUES ('Vetements');
INSERT INTO categorie (nom) VALUES ('Livres');
INSERT INTO categorie (nom) VALUES ('Sport');
INSERT INTO categorie (nom) VALUES ('Maison');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Smartphone Samsung Galaxy', 'Smartphone Android 128Go', 599.99, 50, TRUE, 1,
        'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Laptop Dell XPS 15', 'Intel i7, 16Go RAM, SSD 512Go', 1299.99, 20, TRUE, 1,
        'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Ecouteurs Sony WH-1000XM5', 'Casque sans fil reduction bruit', 349.99, 35, TRUE, 1,
        'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('T-Shirt Premium Coton', 'T-Shirt 100% coton bio', 29.99, 100, TRUE, 2,
        'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Veste en Jean Classic', 'Veste denim vintage', 89.99, 45, TRUE, 2,
        'https://images.unsplash.com/photo-1576995853123-5a10305d93c0?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Java 17 - Guide Complet', 'Reference Java moderne', 45.00, 30, TRUE, 3,
        'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Spring Boot en Action', 'Spring Boot 3', 52.00, 25, TRUE, 3,
        'https://images.unsplash.com/photo-1532012197267-da84d127e765?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Velo de Course Carbon', 'Cadre carbone leger', 1899.99, 8, TRUE, 4,
        'https://images.unsplash.com/photo-1485965120184-e220f721d03e?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Tapis de Yoga Premium', 'Tapis antiderapant 6mm', 49.99, 60, TRUE, 4,
        'https://images.unsplash.com/photo-1601925228717-06717f6a9284?w=400');

INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES ('Machine a Cafe Deluxe', 'Cafetiere avec broyeur', 299.99, 15, TRUE, 5,
        'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400');