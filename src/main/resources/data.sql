INSERT INTO interest (name, description) VALUES
('Sport', 'Zainteresowania sportowe i aktywność fizyczna'),
('Muzyka', 'Pasja do muzyki i koncertów'),
('Film', 'Miłośnik kina i seriali'),
('Podróże', 'Odkrywanie świata i nowych miejsc'),
('Gotowanie', 'Kulinarne eksperymenty'),
('Książki', 'Czytanie literatury'),
('Sztuka', 'Galerie, muzea, malarstwo'),
('Technologia', 'IT, gadżety, innowacje'),
('Fotografia', 'Pasja do robienia zdjęć'),
('Gaming', 'Gry komputerowe i planszowe'),
('Taniec', 'Różne style tańca'),
('Joga', 'Medytacja i mindfulness'),
('Zwierzęta', 'Miłość do zwierząt'),
('Moda', 'Styl i trendy'),
('Natura', 'Wędrówki i outdoor')
ON DUPLICATE KEY UPDATE name=name;

