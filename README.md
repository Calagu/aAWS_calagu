# aAWS · API REST de biblioteca en AWS EC2

**Carlos Larrea Guerrero** · usuario `calagu` · 2.º DAM · Acceso a Datos

Laboratorio 1: una API REST hecha con Spring Boot y una base de datos MySQL, las dos desplegadas en una instancia EC2 de AWS.

Mi variante es la **1**: máximo **3 préstamos abiertos** por socio, **14 días** de préstamo, y un informe de los libros más prestados (`GET /api/informes/mas-prestados?limite=n`).

## Cómo está montado

Todo vive en una sola instancia EC2 (`ad-calagu`, t3.small, Ubuntu):

- La **API** escucha en el puerto 8080 y está abierta solo desde mi IP.
- **MySQL** escucha solo en `127.0.0.1`, así que desde fuera no se ve. Para mirar la base de datos con DBeaver entro por un túnel SSH.
- El grupo de seguridad solo deja entrar por el 22 (SSH) y el 8080 (API). El 3306 no está abierto.
- Tanto MySQL como la API arrancan solos con el sistema (la API es un servicio de systemd).
- Hay una copia de seguridad diaria de la base de datos, con retención de 7 días.
- La instancia tiene una IP elástica, para que la dirección no cambie al reiniciar.

## Qué hay en el repositorio

| Carpeta / archivo | Qué es |
|---|---|
| `biblioteca-api/` | El proyecto Spring Boot (Java 21, Maven) |
| `sql/` | Esquema, datos de prueba, usuarios (plantilla sin contraseñas) y verificación |
| `infra/` | Scripts para preparar la instancia, instalar MySQL, crear el servicio, las copias y desplegar |
| `memoria.pdf` | La memoria con las capturas y las respuestas a las cuestiones |
| `REPOSITORIO.txt` | Enlace a este repositorio y acceso del profesor |

## Probarlo

Con la instancia encendida:

```
curl.exe http://52.45.53.241:8080/api/estado
```

Devuelve el estado de la API, mi usuario (`calagu`), los parámetros de la variante y la versión de MySQL.

El informe de la variante:

```
curl.exe "http://52.45.53.241:8080/api/informes/mas-prestados?limite=5"
```

Para entrar por SSH, `infra/conectar.ps1`. Para abrir el túnel hacia la base de datos, `infra/tunel.ps1` (después conecto DBeaver a `127.0.0.1:3307`).

## Lo que no está aquí (a propósito)

Ni la clave `.pem`, ni el archivo de entorno con las contraseñas, ni la carpeta `target/`. En `infra/` hay una plantilla (`biblioteca.env.plantilla`) y en `sql/` otra para los usuarios, con las contraseñas sin rellenar.

## Notas

Usé Ubuntu 26.04 (con MySQL 8.4) en vez de la 24.04 que pedía el enunciado, con permiso del profesor. Los detalles y los problemas que me fueron saliendo están en la memoria.
