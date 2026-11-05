-- 04_verificacion.sql · Comprobacion del estado de la base de datos.  sudo mysql biblioteca < 04_verificacion.sql
SET NAMES utf8mb4;
USE biblioteca;

SELECT 'autores' AS tabla, COUNT(*) AS filas FROM autor
UNION ALL SELECT 'libros',    COUNT(*) FROM libro
UNION ALL SELECT 'socios',    COUNT(*) FROM socio
UNION ALL SELECT 'prestamos', COUNT(*) FROM prestamo;

-- Prestamos por estado (esperado: DEVUELTO 7, ACTIVO 5, VENCIDO 3)
SELECT CASE
           WHEN fecha_devolucion IS NOT NULL THEN 'DEVUELTO'
           WHEN fecha_limite < CURDATE()     THEN 'VENCIDO'
           ELSE 'ACTIVO'
       END AS estado,
       COUNT(*) AS prestamos
FROM prestamo
GROUP BY estado
ORDER BY estado;

-- Socios con prestamos vencidos (esperado: socios 4 y 5)
SELECT DISTINCT socio_id FROM prestamo
WHERE fecha_devolucion IS NULL AND fecha_limite < CURDATE()
ORDER BY socio_id;

-- Ejemplares disponibles = ejemplares - prestamos abiertos (el libro 4 debe tener 0)
SELECT l.id, l.titulo, l.ejemplares,
       l.ejemplares - (SELECT COUNT(*) FROM prestamo p
                       WHERE p.libro_id = l.id AND p.fecha_devolucion IS NULL) AS disponibles
FROM libro l
WHERE l.id IN (1, 4, 9)
ORDER BY l.id;
