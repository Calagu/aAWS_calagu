-- 02_datos.sql · Datos ficticios de partida: 10 autores, 20 libros, 8 socios y 15 prestamos.
-- Las fechas se calculan respecto al dia de ejecucion.   sudo mysql biblioteca < 02_datos.sql
SET NAMES utf8mb4;
USE biblioteca;

INSERT INTO autor (nombre, nacionalidad, anio_nacimiento) VALUES
 ('Gabriel García Márquez', 'Colombia',  1927),   -- 1
 ('Miguel de Cervantes',    'España',    1547),   -- 2
 ('Isabel Allende',         'Chile',     1942),   -- 3
 ('Jorge Luis Borges',      'Argentina', 1899),   -- 4
 ('Laura Esquivel',         'México',    1950),   -- 5
 ('Julio Cortázar',         'Argentina', 1914),   -- 6
 ('Carmen Laforet',         'España',    1921),   -- 7
 ('Mario Vargas Llosa',     'Perú',      1936),   -- 8
 ('Almudena Grandes',       'España',    1960),   -- 9
 ('Federico García Lorca',  'España',    1898);   -- 10

INSERT INTO libro (isbn, titulo, anio_publicacion, genero, ejemplares, autor_id) VALUES
 ('9788400000001', 'Cien años de soledad',            1967, 'Novela', 3, 1),   -- 1
 ('9788400000002', 'El amor en los tiempos del cólera', 1985, 'Novela', 2, 1), -- 2
 ('9788400000003', 'Don Quijote de la Mancha',        1605, 'Novela', 4, 2),   -- 3
 ('9788400000004', 'Novelas ejemplares',              1613, 'Novela', 1, 2),   -- 4 (un solo ejemplar)
 ('9788400000005', 'La casa de los espíritus',        1982, 'Novela', 2, 3),   -- 5
 ('9788400000006', 'Eva Luna',                        1987, 'Novela', 2, 3),   -- 6
 ('9788400000007', 'Ficciones',                       1944, 'Cuento', 3, 4),   -- 7
 ('9788400000008', 'El Aleph',                        1949, 'Cuento', 2, 4),   -- 8
 ('9788400000009', 'Como agua para chocolate',        1989, 'Novela', 2, 5),   -- 9
 ('9788400000010', 'Rayuela',                         1963, 'Novela', 2, 6),   -- 10
 ('9788400000011', 'Bestiario',                       1951, 'Cuento', 2, 6),   -- 11
 ('9788400000012', 'Nada',                            1945, 'Novela', 3, 7),   -- 12
 ('9788400000013', 'La ciudad y los perros',          1963, 'Novela', 2, 8),   -- 13
 ('9788400000014', 'La fiesta del Chivo',             2000, 'Novela', 2, 8),   -- 14
 ('9788400000015', 'Los aires difíciles',             2002, 'Novela', 2, 9),   -- 15
 ('9788400000016', 'Inés y la alegría',               2010, 'Novela', 1, 9),   -- 16
 ('9788400000017', 'Bodas de sangre',                 1933, 'Teatro', 3, 10),  -- 17
 ('9788400000018', 'Romancero gitano',                1928, 'Poesía', 2, 10),  -- 18
 ('9788400000019', 'La casa de Bernarda Alba',        1945, 'Teatro', 2, 10),  -- 19
 ('9788400000020', 'Poeta en Nueva York',             1940, 'Poesía', 1, 10);  -- 20

INSERT INTO socio (nombre, email, fecha_alta) VALUES
 ('Lucía Fernández', 'lucia.fernandez@example.com', CURDATE() - INTERVAL 400 DAY),   -- 1
 ('Marcos Soler',    'marcos.soler@example.com',    CURDATE() - INTERVAL 350 DAY),   -- 2
 ('Paula Ortega',    'paula.ortega@example.com',    CURDATE() - INTERVAL 300 DAY),   -- 3
 ('Daniel Navarro',  'daniel.navarro@example.com',  CURDATE() - INTERVAL 250 DAY),   -- 4 (con vencidos)
 ('Sara Giménez',    'sara.gimenez@example.com',    CURDATE() - INTERVAL 200 DAY),   -- 5 (con vencidos)
 ('Hugo Moreno',     'hugo.moreno@example.com',     CURDATE() - INTERVAL 150 DAY),   -- 6
 ('Marta Ibáñez',    'marta.ibanez@example.com',    CURDATE() - INTERVAL 100 DAY),   -- 7
 ('Iván Cabrera',    'ivan.cabrera@example.com',    CURDATE() -  INTERVAL 50 DAY);   -- 8 (sin prestamos)

-- 15 prestamos con plazo de 14 dias: 7 devueltos, 5 activos y 3 vencidos
INSERT INTO prestamo (libro_id, socio_id, fecha_prestamo, fecha_limite, fecha_devolucion) VALUES
 -- Devueltos (7)
 ( 1, 1, CURDATE() - INTERVAL 60 DAY, CURDATE() - INTERVAL 46 DAY, CURDATE() - INTERVAL 50 DAY),
 ( 2, 2, CURDATE() - INTERVAL 55 DAY, CURDATE() - INTERVAL 41 DAY, CURDATE() - INTERVAL 45 DAY),
 ( 3, 1, CURDATE() - INTERVAL 40 DAY, CURDATE() - INTERVAL 26 DAY, CURDATE() - INTERVAL 30 DAY),
 ( 5, 3, CURDATE() - INTERVAL 35 DAY, CURDATE() - INTERVAL 21 DAY, CURDATE() - INTERVAL 25 DAY),
 ( 8, 6, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 16 DAY, CURDATE() - INTERVAL 20 DAY),
 (14, 7, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 11 DAY, CURDATE() - INTERVAL 15 DAY),
 ( 1, 8, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL  6 DAY, CURDATE() - INTERVAL 10 DAY),
 -- Activos (5): sin devolver y dentro de plazo
 ( 4, 2, CURDATE() - INTERVAL  4 DAY, CURDATE() + INTERVAL 10 DAY, NULL),   -- libro 4: su unico ejemplar esta prestado
 ( 7, 3, CURDATE() - INTERVAL  3 DAY, CURDATE() + INTERVAL 11 DAY, NULL),
 (10, 6, CURDATE() - INTERVAL  8 DAY, CURDATE() + INTERVAL  6 DAY, NULL),
 (12, 7, CURDATE() - INTERVAL  2 DAY, CURDATE() + INTERVAL 12 DAY, NULL),
 (15, 1, CURDATE() - INTERVAL  1 DAY, CURDATE() + INTERVAL 13 DAY, NULL),
 -- Vencidos (3): sin devolver y con fecha limite pasada (socios 4 y 5)
 ( 9, 4, CURDATE() - INTERVAL 30 DAY, CURDATE() - INTERVAL 16 DAY, NULL),
 (11, 4, CURDATE() - INTERVAL 25 DAY, CURDATE() - INTERVAL 11 DAY, NULL),
 (13, 5, CURDATE() - INTERVAL 20 DAY, CURDATE() - INTERVAL  6 DAY, NULL);
