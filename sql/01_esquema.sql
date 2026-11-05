-- 01_esquema.sql · Base de datos "biblioteca" (MySQL 8.0) · Modelo de la figura 3 del enunciado.
-- Ejecutar como administrador del servidor:  sudo mysql < 01_esquema.sql
SET NAMES utf8mb4;

DROP DATABASE IF EXISTS biblioteca;
CREATE DATABASE biblioteca CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE biblioteca;

CREATE TABLE autor (
    id               INT          NOT NULL AUTO_INCREMENT,
    nombre           VARCHAR(120) NOT NULL,
    nacionalidad     VARCHAR(60)  NOT NULL,
    anio_nacimiento  SMALLINT     NULL,
    CONSTRAINT pk_autor PRIMARY KEY (id)
) ENGINE=InnoDB;

CREATE TABLE libro (
    id                INT          NOT NULL AUTO_INCREMENT,
    isbn              CHAR(13)     NOT NULL,
    titulo            VARCHAR(200) NOT NULL,
    anio_publicacion  SMALLINT     NULL,
    genero            VARCHAR(40)  NULL,
    ejemplares        SMALLINT     NOT NULL,
    autor_id          INT          NOT NULL,
    CONSTRAINT pk_libro PRIMARY KEY (id),
    CONSTRAINT uq_libro_isbn UNIQUE (isbn),
    CONSTRAINT ck_libro_ejemplares CHECK (ejemplares >= 0),
    CONSTRAINT fk_libro_autor FOREIGN KEY (autor_id) REFERENCES autor (id)
) ENGINE=InnoDB;

CREATE TABLE socio (
    id          INT          NOT NULL AUTO_INCREMENT,
    nombre      VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    fecha_alta  DATE         NOT NULL,
    CONSTRAINT pk_socio PRIMARY KEY (id),
    CONSTRAINT uq_socio_email UNIQUE (email)
) ENGINE=InnoDB;

-- Un prestamo con fecha_devolucion NULL esta abierto; si ademas fecha_limite < hoy, esta vencido.
CREATE TABLE prestamo (
    id                INT  NOT NULL AUTO_INCREMENT,
    libro_id          INT  NOT NULL,
    socio_id          INT  NOT NULL,
    fecha_prestamo    DATE NOT NULL,
    fecha_limite      DATE NOT NULL,
    fecha_devolucion  DATE NULL,
    CONSTRAINT pk_prestamo PRIMARY KEY (id),
    CONSTRAINT fk_prestamo_libro FOREIGN KEY (libro_id) REFERENCES libro (id),
    CONSTRAINT fk_prestamo_socio FOREIGN KEY (socio_id) REFERENCES socio (id),
    CONSTRAINT ck_prestamo_limite CHECK (fecha_limite >= fecha_prestamo),
    CONSTRAINT ck_prestamo_devolucion CHECK (fecha_devolucion IS NULL OR fecha_devolucion >= fecha_prestamo),
    INDEX ix_prestamo_libro (libro_id, fecha_devolucion),
    INDEX ix_prestamo_socio (socio_id, fecha_devolucion)
) ENGINE=InnoDB;
