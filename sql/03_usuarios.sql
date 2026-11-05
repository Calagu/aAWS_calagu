-- 03_usuarios.sql · PLANTILLA: sustituye los dos marcadores por contrasenas propias (>= 12 caracteres).
-- En la practica lo hace infra/instalar_mysql.sh pidiendolas por teclado (no se guardan en ningun fichero).
-- NO subas este fichero con contrasenas reales.
CREATE USER IF NOT EXISTS 'biblioteca_app'@'localhost'   IDENTIFIED BY '<CONTRASENA_APP>';
CREATE USER IF NOT EXISTS 'biblioteca_admin'@'localhost' IDENTIFIED BY '<CONTRASENA_ADMIN>';

-- La API: solo manipula datos
GRANT SELECT, INSERT, UPDATE, DELETE ON biblioteca.* TO 'biblioteca_app'@'localhost';
-- Administracion: todos los privilegios, pero solo sobre el esquema biblioteca
GRANT ALL PRIVILEGES ON biblioteca.* TO 'biblioteca_admin'@'localhost';
FLUSH PRIVILEGES;
