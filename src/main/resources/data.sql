-- Supprimer les anciennes données
DELETE FROM produit;
DELETE FROM categorie;

-- ═══════════════════════════════════════════
--  CATÉGORIES
-- ═══════════════════════════════════════════
INSERT INTO categorie (nom) VALUES
('Maquillage Luxe'),    -- id 1
('Soins et Eclat'),     -- id 2
('Parfums Exception'),  -- id 3
('Accessoires Beauty'), -- id 4
('Bien-etre Spa');      -- id 5

-- ═══════════════════════════════════════════
--  PRODUITS  (15 articles)
-- ═══════════════════════════════════════════
INSERT INTO produit (nom, description, prix, stock, actif, categorie_id, image_url)
VALUES

-- ── 1. Rouge à Lèvres Velours ──────────────────────────────────────────────
-- Photo : tubes de rouge à lèvres rouges en or (Valeria Boltneva / Pexels)
('Rouge a Levres ',
 'Fini mat ultra confortable, tenue 12h. Couleur emblematique Rose Eternel -HOUDA BEAUTY-.',
 32.99, 45, TRUE, 1,
 'https://imgs.search.brave.com/i5iwnu4GHiRs0GyZrOmC44SWwCE27MlhlOzG7QEFaGo/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9zaW1w/bGVtZW50bWFyaWx5/bmUuY29tL3dwLWNv/bnRlbnQvdXBsb2Fk/cy8yMDE2LzA5L2h1/ZGEtYmVhdXR5LWxp/cXVpZC1tYXR0ZS1m/YW1vdXMtdHJvcGh5/LXdpZmUuanBnP3c9/NzAwJmg9Mzc1'),

-- ── 2. Coffret Ombres Couture ──────────────────────────────────────────────
-- Photo : palette de fards à paupières colorée ouverte (Suzy Hazelwood / Pexels)
('Coffret Ombres Couture',
 'Palette 12 nuances de la plus douce a la plus intense. Finis mats, satines et irises.-Givenchy-',
 59.99, 28, TRUE, 1,
 'https://imgs.search.brave.com/JsTYp5OlMbxDr6HBQd2lGUy4RBc9T6JRakaNsPIeMXI/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly93d3cu/Y29zbWV0aWNzLWlu/c2lkZXJzLmNvbS9z/dG9yYWdlLzU1NjE0/OC9jb252ZXJzaW9u/cy9wYWxldHRlLW9t/YnJlcy1jb3V0dXJl/LWdpdmVuY2h5LWxl/bGVnYW5jZS1kZXMt/Y291bGV1cnMtYXUt/c2VydmljZS1kdS1y/ZWdhcmQtZnVsbC53/ZWJw'),

-- ── 3. Sérum Lumière Absolue ───────────────────────────────────────────────
-- Photo : flacon sérum vitamine C sur fond blanc épuré (Karolina Grabowska / Pexels)
('Serum Lumiere Absolue',
 'Concentre d eclat a la Vitamine C pure. Effet bonne mine instantane.',
 79.99, 32, TRUE, 2,
 'https://imgs.search.brave.com/IhmplqVzGp3ZrPK8ocsa6YBZayIzJNn0swHmbpV7QfI/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tLm1l/ZGlhLWFtYXpvbi5j/b20vaW1hZ2VzL0kv/NTFENEFMSDdQNUwu/anBn'),

-- ── 4. Crème Riche Nuit ────────────────────────────────────────────────────
-- Photo : pot de crème visage blanc luxueux (Shiny Diamond / Pexels)
('Creme Riche Nuit',
 'Soin reparateur a l acide hyaluronique et beurre de karite. Peau repulpee au reveil.',
 54.99, 40, TRUE, 2,
 'https://imgs.search.brave.com/IL8DfA6qLKSv1uxji064twY2d4WRxTLjsm-whxM2SH8/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly93d3cu/aG9saWRlcm1pZS5j/b20vY2RuL3Nob3Av/cHJvZHVjdHMvQ3Jl/bWVyaWNoZUxJRlQy/XzIwNDh4LmpwZz92/PTE2OTU5MTQzNDM'),

-- ── 5. Eau de Parfum Signature ────────────────────────────────────────────
-- Photo : flacon de parfum élégant sur fond sombre (Valeria Boltneva / Pexels)
('Eau de Parfum Signature',
 'Notes envoutantes de jasmin, vanille et bois de santal. Flacon sculptural.',
 99.99, 25, TRUE, 3,
 'https://imgs.search.brave.com/fmpTUBnwxDqwmTZ5p-YswevyOzlwBc9g7kKHZ0gTFh8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/bW9udGJsYW5jLWJv/dXRpcXVlLWNhbm5l/cy5jb20vY2RuL3No/b3AvcHJvZHVjdHMv/ZWF1LWRlLXBhcmZ1/bW1vbnRibGFuY21v/bnRibGFuYy1zaWdu/YXR1cmUtZWF1LWRl/LXBhcmZ1bS05MC1t/bC04MDczMDEuanBn/P3Y9MTc0MzE2MzM2/MCZ3aWR0aD03NTA'),

-- ── 6. Mascara Volume Extrême ─────────────────────────────────────────────
-- Photo : mascara noir tube + brosse (Shiny Diamond / Pexels)
('Mascara Volume Extreme',
 'Brosse innovante pour un volume spectaculaire sans paquets. Noir intense.',
 27.99, 55, TRUE, 1,
 'https://imgs.search.brave.com/UHG_BZOgf_tY_RmluuBNlGzth0La6D2bPpxIKiSN48o/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly9pNS53/YWxtYXJ0aW1hZ2Vz/LmNvbS9zZW8vQ29z/bm92YS1Fc3NlbmNl/LUktTG92ZS1FeHRy/ZW1lLU1hc2NhcmEt/MC00LW96XzVhOGYw/ZjNiLTE0ZTItNGZl/My1iNjMxLTI5ZTk5/MWU4MGE0NS42NzVj/M2FmMzE3NjNmNjU0/OGQ0ZDE5MzA1OWJk/YjczZC5qcGVn'),

-- ── 7. Set Pinceaux Prestige ──────────────────────────────────────────────
-- Photo : pinceaux maquillage professionnels alignés (Suzy Hazelwood / Pexels)
('Set Pinceaux ',
 '5 pinceaux professionnels en poils synthetiques haut de gamme. Etui inclus.',
 49.99, 38, TRUE, 4,
 'https://imgs.search.brave.com/9IPwORJkRfjooxHnxmkQv0eyuOJIUGo7N2Xlqdgkk18/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pOC5h/bXBsaWVuY2UubmV0/L2kvbWFub3IvMTAw/MDMzMDIzNDFfMDE_/Zm10PWF1dG8maD0z/NjAmdz0zNjA'),

-- ── 8. Eau Micellaire Démaquillante ──────────────────────────────────────
-- Photo : flacon d'eau micellaire transparent (Karolina Grabowska / Pexels)
('Eau Micellaire Demaquillante',
 'Nettoie, demaquille et tonifie en une etape. Peau nette et fraiche.',
 24.99, 62, TRUE, 2,
 'https://imgs.search.brave.com/vYxCsr8uAANIfHt6D2i_aBhuAGnTyLkVEj8qIF3qhi4/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly9jYXVk/YWxpZS1ldXJvcGUu/aW1naXgubmV0L21l/ZGlhL2NhdGFsb2cv/cHJvZHVjdC8zL18v/M192aW5vY2xlYW5f/ZWF1X21pY2VsbGFp/cmVfaW5ncmVkaWVu/dHNfY2F1ZGFsaWUt/ZnJfLXYyLmpwZz9h/dXRvPWZvcm1hdCxj/b21wcmVzcyZjcz1z/cmdiJmZtPWF1dG8m/dz0xMjAw'),

-- ── 9. Bougie Parfumée Figue & Santal ────────────────────────────────────
-- Photo : bougie parfumée allumée ambiance cosy (Taryn Elliott / Pexels)
('Parfum Interieur Bougie',
 'Bougie parfumee a la figue et au santal. 50h de combustion.',
 39.99, 70, TRUE, 5,
 'https://imgs.search.brave.com/dnIhxhDZpX8a04iQjd6XEMmdbQiMjwtgJuah_i0gKo8/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/Y29sbGluZXNkZXBy/b3ZlbmNlLmNvbS84/NDgtaG9tZV9kZWZh/dWx0L3BhcmZ1bS1k/LWludGVyaWV1ci1h/bG9lLXZlcmEuanBn'),

-- ── 10. Gommage Visage Raffinant ─────────────────────────────────────────
-- Photo : produit exfoliant visage texture crémeuse (Shiny Diamond / Pexels)
('Gommage Visage Raffinant',
 'Exfoliant doux aux grains de jojoba. Peau lisse et lumineuse.',
 34.99, 48, TRUE, 2,
 'https://imgs.search.brave.com/e-fEoTauxt0oZz3YE2UIeYC6-wXS0jykbTg4Gu82ZM0/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9sYWtw/dXJhLmNvbS9jZG4v/c2hvcC9wcm9kdWN0/cy9MUzgwMTBDQkRE/LTA2LUVfMS5qcGc_/dj0xNjc2MjIzOTA1/JndpZHRoPTE0NDU'),

-- ── 11. Fond de Teint Peau Parfaite ──────────────────────────────────────
-- Photo : flacon de fond de teint avec pinceau (Karolina Grabowska / Pexels)
('Fond de Teint Peau MAC',
 'Couvrance modulable longue tenue 24h. Fini naturel et lumineux. 30 teintes.',
 44.99, 52, TRUE, 1,
 'https://imgs.search.brave.com/3OPohgNhCPONOdvV53YmeZYLAAxXynttzabrIqgnkKI/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly9zZGNk/bi5pby9tYy9tYWNf/c2t1X1NNWFQxOV9G/Ul8xeDFfNS5wbmc_/aGVpZ2h0PTcwMHB4/JndpZHRoPTcwMHB4'),

-- ── 12. Huile Visage Eclat Doré ──────────────────────────────────────────
-- Photo : huile dorée dans flacon pipette (Shiny Diamond / Pexels)
('Huile Visage Eclat Dore',
 'Huile seche a l argan et au jojoba. Nourrit, illumine et protege. Peaux seches.',
 67.99, 30, TRUE, 2,
 'https://imgs.search.brave.com/xwjXrKnTc_eR-GdM3sGY0aeuvrY3XbtnmMAvnYGE09s/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/ZGFzcGFyZnVtLWJl/YXV0eS5jb20vY2Ru/L3Nob3AvZmlsZXMv/R29sZGVuR2xvd0Jv/ZHlPaWxtb29kMi5q/cGc_dj0xNzYyNjE5/MDAyJndpZHRoPTEy/MDA'),

-- ── 13. Parfum Oriental Oud Intense ──────────────────────────────────────
-- Photo : flacon de parfum oriental ambré luxueux (Valeria Boltneva / Pexels)
('Parfum Oriental Oud Intense',
 'Accord boise et epice : oud, rose noire, ambre. Sillage puissant et envoûtant.',
 129.99, 18, TRUE, 3,
 'https://imgs.search.brave.com/-Ns-0rsJ4aVCz8OVYGEpq_ePQEKeehmme7Vumpey-c0/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9ub3Vy/LW9yaWVudC5jb20v/d3AtY29udGVudC91/cGxvYWRzLzIwMjMv/MDEvTXVzY19PVURf/SU5URU5TRV9ub3Vy/X29yaWVudDEtc2Nh/bGVkLTEuanBn'),

-- ── 14. Masque Capillaire Soin Royal ─────────────────────────────────────
-- Photo : produit soin cheveux crème dans pot (Karolina Grabowska / Pexels)
('Masque Capillaire Soin Royal',
 'Masque nutrition intense a la keratine et huile d argan. Cheveux soyeux en 10 min.',
 29.99, 44, TRUE, 5,
 'https://imgs.search.brave.com/DWJ_LD5cz3Xqvv2cyZUU7riPkmYv1ZyPOnHh5jW3aiA/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/Y29tbWUtYXZhbnQu/YmlvL2Nkbi9zaG9w/L2ZpbGVzL3NvaW4t/Y2FwaWxsYWlyZS1w/ZWNoZS12YW5pbGxl/LXN0dWRpby1jb21t/ZS1hdmFudC5qcGc_/dj0xNzY5NTI0MTky/JndpZHRoPTE1MDA'),

-- ── 15. Trousse Maquillage Cuir Vegan ────────────────────────────────────
-- Photo : trousse maquillage élégante avec produits (Suzy Hazelwood / Pexels)
('Trousse Maquillage Cuir Vegan',
 'Trousse spacieuse en simili cuir rose. Compartiments multiples. Miroir integre.',
 42.99, 35, TRUE, 4,
 'https://mlleetcoco.com/cdn/shop/files/JOLIE_MUSE_Carre_P1_1800x1800.jpg?v=1771002914');